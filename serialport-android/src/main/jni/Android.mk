LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
TARGET_PLATFORM := android-16
LOCAL_MODULE    := serial
LOCAL_SRC_FILES := serial.c sercd.c android.c unix.c
LOCAL_CFLAGS    := -DVERSION=\"3.0.0\"
LOCAL_LDLIBS    := -llog
include $(BUILD_SHARED_LIBRARY)
