LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := treble-overlay-lenovo-yoga-smart-tab
LOCAL_MODULE_PATH := $(TARGET_OUT_PRODUCT)/overlay
LOCAL_IS_RUNTIME_RESOURCE_OVERLAY := true
LOCAL_PRIVATE_PLATFORM_APIS := true
include $(BUILD_PACKAGE)
