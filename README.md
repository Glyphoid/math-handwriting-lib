# math-handwriting-lib

===========================================================================
A parser and evaluator of handwritten mathematical formulae (Java)
===========================================================================

[![Build & Test on Travis CI](https://travis-ci.org/Glyphoid/math-handwriting-lib.svg?branch=master)](https://travis-ci.org/Glyphoid/math-handwriting-lib)

See:
* [Web GUI Demo](http://scai.io/glyphoid/)
* [Demo videos (YouTube)](https://www.youtube.com/watch?v=9LFmDcpyZ0w&list=PLcUSYoM0otQi4qCaO5uzluG8ww69kgepc)

This library is for recognition of handwritten mathematical expresssions in the **online** fashion, i.e., through the utilization of stroke representations of handwritten symbols. 

This is the core part of Glyphoid.

List of currently supported math notation syntaxes:

1. Numerical values: Decimal numbers, negative/positive signs
2. Basic arithemtics: Addition, subtraction, multplication
3. Exponentiation
4. Square root
5. Parentheses (Limited support so far)
6. Variable definition and arithmetics: Symbol names including Latin and Greek letters, with or without suffixes
7. Function definition and evaluation: Multi-argument function supported
8. Matrices and vectors: Sparse matrix notations supported, addition, multiplication, transpose, inverse
9. Certain common elementary functions: e.g., sin, cos, log, exp
10. Certain matrix functions: det, rank
11. Summation (Sigma) and product (Pi) expressions
12. Definite integrals
13. Numerical comparisons (e.g., >, <, =) 
14. Logical AND / OR expressions
15. Common mathematical and scientific constants, such as pi, e and c

## Build and Test
1. Make sure that you have the following installed:
  1. Java 7 or above
  2. Apache Maven
2. Download and build the [Glyphoid Java Worker Pool Porject](https://github.com/Glyphoid/java-worker-pool)
3. Get the token set data from the [tokensets repository](https://github.com/Glyphoid/tokensets). These are required by the unit tests of math-handwriting-lib.
4. cd to the root directory of math-handwriting-lib (i.e., where the pom.xml is)
5. On Linux and Mac, execute Maven clean build and local-repository installation: 

    `mvn clean install -DtokenSetPathPrefix=${TOKENSETS_DATA_DIR}/TS_`

    On Windows, do

    `mvn clean install -DtokenSetPathPrefix=${TOKENSETS_DATA_DIR}\TS_`

The environment variable `tokenSetPathPrefix` tells the unit tests in math-handwriting-lib the location of the test token set files. `TOKENSETS_DATA_DIR` is the path to the above-mentioned tokensets repository.
