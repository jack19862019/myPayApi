package com.pay.manager.pc.config;


import com.pay.manager.pc.config.params.SysConfigPrams;

public interface SysConfigService {


    void insertConfig(SysConfigPrams sysConfigPrams);

    void deleteConfig(Long id);

    SysConfigPrams getConfig();

    void updateConfig(Long id, SysConfigPrams sysConfigPrams);
}
