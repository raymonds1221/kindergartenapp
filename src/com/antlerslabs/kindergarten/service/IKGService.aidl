package com.antlerslabs.kindergarten.service;

import com.antlerslabs.kindergarten.service.IKGServiceCallback;

interface IKGService {
	void requestProfiles(int userId);
	void requestSyncCalendar();
	void registerCallback(IKGServiceCallback callback);
	void unregisterCallback(IKGServiceCallback callback);
}