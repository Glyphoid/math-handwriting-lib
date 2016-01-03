# math-handwriting-lib
Java library for parsing and evaluating handwritten mathematical formulae. 

This library is for "online" recognition of handwritten math, i.e., recognition based on stroke information. Stroke representations of handwritten math expressions are recognized and evaluated. 

This is the core part of Glyphoid.

See:
* [Web GUI Demo](http://scai.io/glyphoid/)
* [Demo videos (YouTube)](https://www.youtube.com/watch?v=9LFmDcpyZ0w&list=PLcUSYoM0otQi4qCaO5uzluG8ww69kgepc)


## How to build Glyphoid math-handwriting-lib
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
