export JAVA_HOME=~/jdk8u172-b11
export PATH=$JAVA_HOME/bin:$PATH
export CC=/usr/local/bin/clang
export LD_LIBRARY_PATH=$PWD/tests/build:$LD_LIBRARY_PATH

mkdir -p tests/build
javac -d tests/build tests/com/amazon/aws/test.java
javah -classpath tests/build -d tests/include/aws/jni com.amazon.aws.Test 
pushd tests/build > /dev/null
cmake -GNinja ..
ninja
java com.amazon.aws.Test
popd > /dev/null