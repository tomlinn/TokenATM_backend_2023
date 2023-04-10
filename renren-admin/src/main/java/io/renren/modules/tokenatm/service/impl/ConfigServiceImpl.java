package io.renren.modules.tokenatm.service.impl;

import io.renren.modules.tokenatm.entity.ConfigEntity;
import io.renren.modules.tokenatm.entity.SpendLogEntity;
import io.renren.modules.tokenatm.service.ConfigService;
import io.renren.modules.tokenatm.service.LogRepository;
import io.renren.modules.tokenatm.service.Response.UpdateConfigResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Component("ConfigService")
public class ConfigServiceImpl implements ConfigService {
    @Autowired
    private io.renren.modules.tokenatm.service.ConfigRepository ConfigRepository;

    @Autowired
    private LogRepository LogRepository;

    public UpdateConfigResponse updateConfigEntity(Integer id, String config_name) {
        Optional<ConfigEntity> optional = ConfigRepository.findById(id);
        ConfigEntity entity = null;
        String oriConfigName = "";
        if (optional.isPresent()) {
            entity = optional.get();
            oriConfigName = entity.getConfigName();
            entity.setConfigName(config_name);
            entity.setTimestamp(new Date());
        }
        ConfigRepository.save(entity);
        LogRepository.save(createLog("", "", "system", 0, "update Config from (Name:"+ oriConfigName +") to (Name:" + config_name + ")",""));
        return new UpdateConfigResponse( "Config entity updated successfully");
    }

    @Override
    public UpdateConfigResponse addConfigEntity(String config_type, String config_name) throws JSONException, IOException {
        ConfigEntity entity = new ConfigEntity();
        entity.setConfigType(config_type);
        entity.setConfigName(config_name);
        entity.setTimestamp(new Date());
        ConfigRepository.save(entity);
        LogRepository.save(createLog("", "", "system", 0, "add Config (Type:" + config_type +" Name:"+ config_name +")",""));
        return new UpdateConfigResponse( "Config entity added successfully");
    }

    @Override
    public UpdateConfigResponse deleteConfigEntity(Integer id) throws JSONException, IOException {
        Optional<ConfigEntity> optional = ConfigRepository.findById(id);
        if (!optional.isPresent()) {
            return new UpdateConfigResponse( "Config entity not found");
        }
        ConfigRepository.delete(optional.get());
        LogRepository.save(createLog("", "", "system", 0, "delete Config (Type:" + optional.get().getConfigType() +" Name:"+ optional.get().getConfigName() +")",""));
        return new UpdateConfigResponse("Config entity deleted successfully");
    }

    private SpendLogEntity createLog(String user_id, String user_name, String type, Integer token_count, String source, String note) {
        SpendLogEntity n = new SpendLogEntity();
        n.setUser_id(user_id);
        n.setUser_name(user_name);
        n.setType(type);
        n.setTokenCount(token_count);
        n.setSourcee(source);
        n.setTimestamp(new Date());
        n.setNote(note);
        return n;
    }
}