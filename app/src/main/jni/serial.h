/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class android_serial_port_api_serial_port */
#ifndef _Included_android_serial_port_api_serial_port
#define _Included_android_serial_port_api_serial_port
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     android_serial_port_api_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_android_serial_port_api_SerialPort_open(JNIEnv *, jclass, jstring, jint, jint);
/*
 * Class:     android_serial_port_api_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_android_serial_port_api_SerialPort_close(JNIEnv *, jobject);
#ifdef __cplusplus
}
#endif
#endif
