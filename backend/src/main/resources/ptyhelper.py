#!/usr/bin/env python3
"""
PTY helper: bash を PTY 内で起動し、stdin/stdout を仲介する。
引数: <init_file> <resize_fifo> <cols> <rows>
resize_fifo に "<cols> <rows>\n" を書き込むことでウィンドウサイズを変更できる。
"""
import os, sys, pty, select, fcntl, termios, struct, signal

def set_winsize(fd, rows, cols):
    fcntl.ioctl(fd, termios.TIOCSWINSZ, struct.pack('HHHH', rows, cols, 0, 0))

def main():
    if len(sys.argv) < 5:
        sys.stderr.write("usage: ptyhelper.py <init_file> <resize_fifo> <cols> <rows>\n")
        sys.exit(1)

    init_file   = sys.argv[1]
    resize_fifo = sys.argv[2]
    cols        = int(sys.argv[3])
    rows        = int(sys.argv[4])

    # resize FIFO を非ブロッキング読み取りモードで開く
    resize_fd = os.open(resize_fifo, os.O_RDONLY | os.O_NONBLOCK)

    # PTY を確保しながら bash を fork
    pid, master_fd = pty.fork()

    if pid == 0:
        # 子プロセス: bash を exec (PTY がすでに stdin/stdout/stderr になっている)
        env = dict(os.environ)
        env['TERM']            = 'xterm-256color'
        env['COLORTERM']       = 'truecolor'
        env['FORCE_COLOR']     = '1'
        env['GIT_PAGER']       = 'cat'
        os.execve('/bin/bash',
                  ['/bin/bash', '--init-file', init_file, '--noprofile'],
                  env)
        # ここには到達しない

    # 親プロセス: PTY の初期サイズを設定
    set_winsize(master_fd, rows, cols)

    stdin_fd  = sys.stdin.fileno()
    stdout_fd = sys.stdout.fileno()
    resize_buf = b''

    try:
        while True:
            try:
                r, _, ex = select.select(
                    [stdin_fd, master_fd, resize_fd], [], [master_fd], 0.05)
            except (OSError, ValueError):
                break

            # PTY エラー (bash 終了など)
            if master_fd in ex:
                break

            for fd in r:
                if fd == stdin_fd:
                    try:
                        data = os.read(stdin_fd, 4096)
                        if not data:
                            return
                        os.write(master_fd, data)
                    except OSError:
                        return

                elif fd == master_fd:
                    try:
                        data = os.read(master_fd, 4096)
                        if not data:
                            return
                        os.write(stdout_fd, data)
                    except OSError:
                        return

                elif fd == resize_fd:
                    try:
                        chunk = os.read(resize_fd, 64)
                        resize_buf += chunk
                        while b'\n' in resize_buf:
                            line, resize_buf = resize_buf.split(b'\n', 1)
                            parts = line.strip().split(b' ')
                            if len(parts) == 2:
                                try:
                                    c, r2 = int(parts[0]), int(parts[1])
                                    set_winsize(master_fd, r2, c)
                                    os.kill(pid, signal.SIGWINCH)
                                except (ValueError, ProcessLookupError):
                                    pass
                    except OSError:
                        pass
    finally:
        try:
            os.kill(pid, signal.SIGTERM)
        except Exception:
            pass
        for fd in (master_fd, resize_fd):
            try:
                os.close(fd)
            except Exception:
                pass

main()
