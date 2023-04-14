package io.renren.modules.tokenatm.service;

import io.renren.modules.tokenatm.service.Response.UpdateConfigResponse;
import org.json.JSONException;

import java.io.IOException;

public interface ConfigService {
    UpdateConfigResponse updateConfigEntity(Integer id, String config_name) throws JSONException, IOException;
    UpdateConfigResponse addConfigEntity(String config_type, String config_name) throws JSONException, IOException;
    UpdateConfigResponse deleteConfigEntity(Integer id) throws JSONException, IOException;
}

