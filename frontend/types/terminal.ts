export interface CommitNode {
  hash: string
  shortHash: string
  message: string
  author: string
  timestamp: string
  parents: string[]
}

export interface BranchRef {
  name: string
  hash: string
  isHead: boolean
}

export interface GraphData {
  commits: CommitNode[]
  branches: BranchRef[]
  head: string
}

export interface TerminalCommandResponse {
  output: string
  success: boolean
  graph: GraphData
}
