# Using the machine-learning (ML) module of Glyphoid math-handwriting-lib

Below are steps to train the token engine.

As preparatory steps, install git, Java 7+ and Maven 3+.

```bash
# Clone and build Glyphoid/java-worker-pool
git clone https://github.com/Glyphoid/java-worker-pool.git
pushd java-worker-pool
mvn clean install
popd

# Clone and build Glyphoid/java-web-utils
git clone https://github.com/Glyphoid/java-web-utils.git
pushd java-web-utils
mvn clean install
popd

mkdir /tmp/glyphoid

# Download token data from git hub.
pushd /tmp/glyphoid
git clone https://github.com/Glyphoid/token-data.git
mkdir intm_data
mkdir token_engine
popd

# Build and run math-handwriting-engine's TrainTokenRecogEngineSDV main class.
git clone  https://github.com/Glyphoid/math-handwriting-lib.git
pushd math-handwriting-lib

export TOKEN_DIR="/tmp/glyphoid/token-data"
export DATA_DIR="/tmp/glyphoid/intm_data"
export ENGINE_DIR="/tmp/glyphoid/token_engine"
export NEW_IMAGE_SIZE=32

mvn package -DskipTests && mvn exec:java \
    -Dexec.args="--token_dir ${TOKEN_DIR} "\
"--data_dir ${DATA_DIR} "\
"--engine_dir ${ENGINE_DIR} "\
"--new_image_size ${NEW_IMAGE_SIZE}"

popd
```

If you just need to generate the intermediate data files without training the
token engine with the encog-based java code, you can use the flag
`--generate_intermediate_data_only`:

```bash
mvn package -DskipTests && mvn exec:java \
    -Dexec.args="--token_dir ${TOKEN_DIR} "\
"--data_dir ${DATA_DIR} "\
"--engine_dir ${ENGINE_DIR} "\
"--new_image_size ${NEW_IMAGE_SIZE} "
"--generate_intermediate_data_only true"
```
