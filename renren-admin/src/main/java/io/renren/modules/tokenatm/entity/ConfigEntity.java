package io.renren.modules.tokenatm.entity;

import javax.persistence.*;
import java.util.Date;

@Entity // This tells Hibernate to make a table out of this class
@Table(indexes = @Index(name="id_idx", columnList = "config_type"))
public class ConfigEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	private String config_type;
	private String config_name;
	private Date timestamp;

	public String getConfigType() {
		return config_type;
	}

	public void setConfigType(String config_type) {
		this.config_type = config_type;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getConfigName() {
		return config_name;
	}

	public void setConfigName(String config_name) {
		this.config_name = config_name;
	}

	public void setTimestamp(Date current_time) {
		this.timestamp = current_time;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

}
