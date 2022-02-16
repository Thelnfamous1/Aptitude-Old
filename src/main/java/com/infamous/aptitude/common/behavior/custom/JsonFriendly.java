package com.infamous.aptitude.common.behavior.custom;

import com.google.gson.JsonObject;

public interface JsonFriendly<U> {

    U fromJson(JsonObject jsonObject);
}
