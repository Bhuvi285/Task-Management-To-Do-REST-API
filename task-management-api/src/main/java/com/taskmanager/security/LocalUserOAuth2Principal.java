package com.taskmanager.security;

import com.taskmanager.entity.User;

public interface LocalUserOAuth2Principal {

    User getLocalUser();
}
