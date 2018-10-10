
#include "aws/jni/com_amazon_aws_Test.h"

#include <stdio.h>

void JNICALL Java_com_amazon_aws_Test_doIt(JNIEnv *env, jclass cls) {
    printf("I DID THE THING\n");
}
