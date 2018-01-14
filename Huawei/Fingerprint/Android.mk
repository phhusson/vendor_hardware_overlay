LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := hw-fpnav-daemon
LOCAL_SRC_FILES := \
	input.cpp

LOCAL_CLANG := true
LOCAL_CFLAGS := -Wall -Werror -Wextra

LOCAL_INIT_RC := hw-fingerprint.rc

LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)
