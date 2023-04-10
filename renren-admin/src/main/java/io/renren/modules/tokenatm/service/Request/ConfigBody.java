package io.renren.modules.tokenatm.service.Request;

public class ConfigBody {

    private Integer id;
    private String configName;
    private String configType;

    public ConfigBody() {
        // Default constructor
    }

    public Integer getId() {
        return id;
    }
    public void setId (Integer id) {
        this.id = id;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }


}
