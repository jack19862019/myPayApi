package com.pay.manager.pc.mcp;

import com.pay.common.enums.AmountType;
import com.pay.common.enums.RoleType;
import com.pay.common.exception.Assert;
import com.pay.common.security.SecurityUtils;
import com.pay.data.entity.*;
import com.pay.data.mapper.*;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.mcp.params.*;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@Service
public class McpConfigServiceImpl extends AbstractHelper<McpConfigRepository, McpConfigEntity, Long> implements McpConfigService {

    @Override
    public void insert(McpConfigReqParams reqParams) {
        List<Long> mcpPayTypeIds = reqParams.getMcpPayTypeIds();
        Long channelId = reqParams.getChannelId();
        Optional<ChannelEntity> byId = channelRepository.findById(channelId);
        Assert.mustBeTrue(byId.isPresent(), "通道不存在");
        //List<Long> alreadyIds = byId.get().getPayTypes().stream().map(PayTypeEntity::getId).collect(toList());
        //List<Long> reduceIds = mcpPayTypeIds.stream().filter(item -> !alreadyIds.contains(item)).collect(toList());
        //Assert.mustBeTrue(CollectionUtils.isEmpty(reduceIds), reduceIds + "支付方式不存在通道:" + channelId + "中");
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

    private void checkAmount(McpAmountReqParams reqParams) {
        AmountType amountType = reqParams.getAmountType();
        String amountStr = reqParams.getAmountStr();
        if (!StringUtils.isEmpty(amountStr)) {
            Assert.mustBeTrue(!ObjectUtils.isEmpty(amountType), "金额类型不能为空");
            if (AmountType.FIXED.equals(amountType)){
                isNumeric(amountStr);
            }
            if (AmountType.RANGE.equals(amountType)) {
                String[] split = amountStr.split(",");
                isNumeric(split);
                BigDecimal sm = new BigDecimal(split[0]);
                BigDecimal mx = new BigDecimal(split[0]);
                Assert.mustBeTrue(sm.compareTo(mx) < 0, "更小金额不能大于更大金额");
            }
            if (AmountType.MULTIPLE.equals(amountType)) {
                String[] split = amountStr.split(",");
                isNumeric(split);
                BigDecimal sm = new BigDecimal(split[0]);
                BigDecimal bs = new BigDecimal(split[0]);
                BigDecimal mx = new BigDecimal(split[0]);
                Assert.mustBeTrue(sm.multiply(bs).compareTo(mx) < 0, "必须满足范围与倍数的关系");
                Assert.mustBeTrue(mx.divide(bs).compareTo(sm) > 0, "必须满足范围与倍数的关系");
            }
        }
    }


    private static void isNumeric(String... split) {
        for (String s : split) {
            Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
            Assert.mustBeTrue(pattern.matcher(s).matches(), s + "不是一个整型的数字");
        }
    }

    private void insertMcpBusiness(McpConfigEntity mcpConfigEntity, McpConfigReqParams reqParams) {
        MerchantEntity merchant = getMerchant(reqParams.getMerchantId());
        ChannelEntity channel = getChannel(reqParams.getChannelId());

        mcpConfigEntity.setMerchant(merchant);
        mcpConfigEntity.setChannel(channel);

        mcpConfigEntity.setCreateUser(merchant.getMerchantNo());
        save(mcpConfigEntity);
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
}
