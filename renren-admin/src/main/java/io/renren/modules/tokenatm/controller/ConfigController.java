package io.renren.modules.tokenatm.controller;

import io.renren.modules.tokenatm.entity.ConfigEntity;
import io.renren.modules.tokenatm.service.ConfigRepository;
import io.renren.modules.tokenatm.service.ConfigService;
import io.renren.modules.tokenatm.service.Request.ConfigBody;
import io.renren.modules.tokenatm.service.Request.RequestLogBody;
import io.renren.modules.tokenatm.service.Response.UpdateConfigResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
// This means that this class is a Controller
public class ConfigController {
    @Autowired // This means to get the bean called LogRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private ConfigRepository configRepository;

    @Autowired
    ConfigService configService;

    @GetMapping(path="/config")
    public @ResponseBody Iterable<ConfigEntity> getLogs() {
        // This returns a JSON or XML with the logs
        return configRepository.findAll();
    }

    @PostMapping(path="/config/")
    public @ResponseBody List<String> getLogsForStudent(@RequestBody RequestLogBody body) {
        return configRepository.findByType(body.getName());
    }
    @PostMapping("/config/update")
    public @ResponseBody UpdateConfigResponse updateConfig(@RequestBody ConfigBody body) throws JSONException, IOException {
        return configService.updateConfigEntity(body.getId(), body.getConfigName());
    }

    @PostMapping("/config/add")
    public @ResponseBody UpdateConfigResponse addConfig(@RequestBody ConfigBody body) throws JSONException, IOException {
        return configService.addConfigEntity(body.getConfigType(), body.getConfigName());
    }

    @PostMapping("/config/delete")
    public @ResponseBody UpdateConfigResponse deleteToken(@RequestBody ConfigBody body) throws JSONException, IOException {
        return configService.deleteConfigEntity(body.getId());
    }
}
