#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <linux/input.h>

int open_fingerprint() {
	DIR *dir=opendir("/dev/input");
	if(!dir) {
		return -1;
	}
	int fd;
	struct dirent *entry;
	while( (entry=readdir(dir))!=NULL) {
		if(entry->d_type!=DT_CHR)
			continue;
		if(strncmp(entry->d_name, "event", 5)==0) {
			fd = openat(dirfd(dir), entry->d_name, O_RDONLY);
			if(fd < 0) continue;

			char buf[64];
			int ret = ioctl(fd, EVIOCGNAME(sizeof(buf)), buf);
			if(ret < 0) {
				close(fd);
				continue;
			}

			if(strcmp(buf, "fingerprint") != 0) {
				close(fd);
				continue;
			}

			ioctl(fd, EVIOCGRAB, 1);
			closedir(dir);
			return fd;
			break;
		}
	}
	closedir(dir);
	return -1;
}

#define FINGERPRINT_CLICK	0xae
#define FINGERPRINT_LONGPRESS	0x1c
#define FINGERPRINT_RIGHT	0x6a
#define FINGERPRINT_LEFT	0x69
#define FINGERPRINT_UP		0x67
#define FINGERPRINT_DOWN	0x6c
#define FINGERPRINT_DOUBLECLICK 0x6f
int main() {
	int fd = open_fingerprint();
	if(fd<0) return 1;

	struct input_event ev;
	while(read(fd, &ev, sizeof(ev)) == sizeof(ev)) {
		if(ev.type != EV_KEY) continue;
		//Huawei kernel code automatically generates both up and down events, just take one
		if(ev.value != 1) continue; 

		switch(ev.code) {
			case FINGERPRINT_CLICK:
				system("input keyevent KEYCODE_HOME &");
				break;
			case FINGERPRINT_LEFT:
				system("input keyevent KEYCODE_BACK &");
				break;
			case FINGERPRINT_RIGHT:
				system("input keyevent KEYCODE_VOICE_ASSIST &");
				break;
			case FINGERPRINT_UP:
				system("cmd statusbar expand-settings &");
				break;
			case FINGERPRINT_DOWN:
				system("cmd statusbar expand-notifications &");
				break;
			case FINGERPRINT_LONGPRESS:
				system("input keyevent KEYCODE_APP_SWITCH &");
				break;
		};
	}
}
