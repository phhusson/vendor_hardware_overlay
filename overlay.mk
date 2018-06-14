PRODUCT_PACKAGES += \
	treble-overlay-asus-zenfone5 \
	treble-overlay-huawei \
	treble-overlay-huawei-telephony \
	treble-overlay-NavBar \
	treble-overlay-NightMode \
	treble-overlay-Telephony-LTE \
	treble-overlay-SystemUI-FalseLocks \
	treble-overlay-devinputjack \
	treble-overlay-oneplus-op6 \
	treble-overlay-xiaomi-mi8 \
	treble-overlay-xiaomi-mimix2s \
	HardwareOverlayPicker \
	QtiAudio \
	Touchscreen


PRODUCT_PACKAGES += \
	hw-fpnav-daemon
PRODUCT_COPY_FILES += \
	vendor/hardware_overlay/Huawei/Fingerprint/hw-fingerprint.rc:system/etc/init/hw-fingerprint.rc \
	vendor/hardware_overlay/Huawei/Fingerprint/hw-fpnav:system/bin/hw-fpnav \
	vendor/hardware_overlay/Huawei/Fingerprint/hw-fpnav.dex:system/phh/hw-fpnav.dex \
	vendor/hardware_overlay/Huawei/Fingerprint/fingerprint.kl:system/phh/huawei/fingerprint.kl
