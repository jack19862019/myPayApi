package com.pay.manager.pc.mcp;

import com.pay.common.exception.Assert;
import com.pay.data.entity.*;
import com.pay.data.mapper.*;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.mcp.params.*;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class McpConfigServiceImpl extends AbstractHelper<McpConfigRepository, McpConfigEntity, Long> implements McpConfigService {

    @Override
    public void insert(McpConfigReqParams reqParams) {
        Long channelId = reqParams.getChannelId();
        Optional<ChannelEntity> byId = channelRepository.findById(channelId);
        Assert.mustBeTrue(byId.isPresent(), "通道不存在");
        McpConfigEntity mcpConfigEntity = BeanCopyUtils.copyBean(reqParams, McpConfigEntity.class, McpConfigOption.class);
        insertMcpBusiness(mcpConfigEntity, reqParams);
    }

    @Override
    public McpConfigDetailParams select(Long id) {
        return BeanCopyUtils.copyBean(getById(id), McpConfigDetailParams.class);
    }

    @Override
    public void update(McpConfigReqParams reqParams) {
        MerchantEntity merchant = getMerchant(reqParams.getMerchantId());
        ChannelEntity channel = getChannel(reqParams.getChannelId());
        McpQuery mcpQuery = new McpQuery();
        mcpQuery.setChannelId(channel.getId());
        mcpQuery.setMerchantId(merchant.getId());
        List<McpConfigEntity> mcp = getList(mcpQuery, new Sort(Sort.Direction.DESC, "merchant_id"));
        Assert.mustBeTrue(!CollectionUtils.isEmpty(mcp) && mcp.size() == 1, "商户通道配置有且只有一个");
        McpConfigEntity mcpConfigEntity = BeanCopyUtils.copyBean(reqParams, mcp.get(0), McpConfigOption.class);
        insertMcpBusiness(mcpConfigEntity, reqParams);
    }

    @Override
    public void delete(Long id) {
        deleteById(id);
    }

    @Override
    public McpConfigRespParams getMcpChannels(Long merchantId) {
        List<McpConfigEntity> configEntities = getList(new McpQuery(merchantId), true);
        if (CollectionUtils.isEmpty(configEntities)) {
            Optional<MerchantEntity> merchant = merchantRepository.findById(merchantId);
            Assert.mustBeTrue(merchant.isPresent(), "商户:" + merchantId + "不存在");
            return new McpConfigRespParams(BeanCopyUtils.copyBean(merchant.get(), McpMerchantParams.class), new ArrayList<>());
        }
        List<ChannelEntity> channels = configEntities.stream().map(McpConfigEntity::getChannel).collect(toList());
        List<McpChannelParams> channelParams = BeanCopyUtils.copyList(new ArrayList<>(channels), McpChannelParams.class);
        McpMerchantParams mcpMerchantParams = BeanCopyUtils.copyBean(configEntities.get(0).getMerchant(), McpMerchantParams.class);
        return new McpConfigRespParams(mcpMerchantParams, channelParams);
    }

    @Override
    public McpConfigDetailParams getMcpChannelDetail(McpQuery mcpQuery) {
        getMerchant(mcpQuery.getMerchantId());//只做检查
        getChannel(mcpQuery.getChannelId());//只做检查
        List<McpConfigEntity> mcp = getList(mcpQuery, new Sort(Sort.Direction.DESC, "merchant_id"));
        Assert.mustBeTrue(!CollectionUtils.isEmpty(mcp) && mcp.size() == 1, "商户通道配置有且只有一个");
        return BeanCopyUtils.copyBean(mcp.get(0), McpConfigDetailParams.class);
    }

    private void insertMcpBusiness(McpConfigEntity mcpConfigEntity, McpConfigReqParams reqParams) {
        MerchantEntity merchant = getMerchant(reqParams.getMerchantId());
        ChannelEntity channel = getChannel(reqParams.getChannelId());

        mcpConfigEntity.setMerchant(merchant);
        mcpConfigEntity.setChannel(channel);
        mcpConfigEntity.setCreateUser(merchant.getMerchantNo());

        mcpConfigEntity.getMcpPayType().clear();
        mcpConfigEntity.getMcpPayType().addAll(buildMcpPayType(mcpConfigEntity, reqParams));

        save(mcpConfigEntity);
    }

    private Set<McpPayTypeEntity> buildMcpPayType(McpConfigEntity mcpConfigEntity, McpConfigReqParams reqParams) {
        Optional<ChannelEntity> byId = channelRepository.findById(reqParams.getChannelId());
        Assert.mustBeTrue(byId.isPresent(), "通道不存在");
        Set<UpPayTypeEntity> upPayTypes = byId.get().getUpPayTypes();

        Set<McpPayTypeEntity> mcpPayTypeEntities = new HashSet<>();
        for (UpPayTypeEntity upPayType : upPayTypes){
            PayTypeEntity payType = upPayType.getPayType();
            if (reqParams.getMcpPayTypeIds().contains(payType.getId())){
                McpPayTypeEntity mcpPayTypeEntity = new McpPayTypeEntity();
                mcpPayTypeEntity.setUpPayType(upPayType);
                mcpPayTypeEntity.setMcpConfig(mcpConfigEntity);
                mcpPayTypeEntities.add(mcpPayTypeEntity);
            }
        }
        return mcpPayTypeEntities;
    }

    private MerchantEntity getMerchant(Long merchantId) {
        Optional<MerchantEntity> merchant = merchantRepository.findById(merchantId);
        Assert.mustBeTrue(merchant.isPresent(), merchantId + "商户不存在！");
        return merchant.get();
    }

    private ChannelEntity getChannel(Long channelId) {
        Optional<ChannelEntity> channel = channelRepository.findById(channelId);
        Assert.mustBeTrue(channel.isPresent(), channelId + "通道不存在！");
        return channel.get();
    }

    @Autowired
    MerchantRepository merchantRepository;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    PayTypeRepository payTypeRepository;

    @Autowired
    McpConfigRepository mcpConfigRepository;

    @Autowired
    McpPayTypeRepository mcpPayTypeRepository;
}
