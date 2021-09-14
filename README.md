# About this repository
Codes in this repository parses a program written in a lustre grammar and build a parsing tree. The parsing tree is used to analyze how errors flow from input nodes to the final output node, so we call it 'Error Propagation Tree'.

It takes a lustre file and a test suite file for the lustre program as inputs and yields error flow information as output.

This code is a base code for experiments performed in the following paper.

@INPROCEEDINGS{ErrorpropagationTree,
  author={Joo, Jeonghyun and Yoo, Seunghoon and Park, Myunghwan},
  booktitle={2020 IEEE 13th International Conference on Software Testing, Validation and Verification (ICST)}, 
  title={Poster: Test Case Prioritization Using Error Propagation Probability}, 
  year={2020},
  pages={398-401}
}
