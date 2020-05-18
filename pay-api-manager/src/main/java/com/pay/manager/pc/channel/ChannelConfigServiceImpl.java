package com.pay.manager.pc.channel;

import com.pay.common.annotation.Remark;
import com.pay.common.exception.Assert;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.ChannelReqParamsEntity;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.mapper.ChannelReqParamsRepository;
import com.pay.data.params.OrderReqParams;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.channel.params.ChannelConfigIndexParams;
import com.pay.manager.pc.channel.params.ChannelConfigReqParams;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class ChannelConfigServiceImpl extends AbstractHelper<ChannelReqParamsRepository, ChannelReqParamsEntity, Long> implements ChannelConfigService {

    @Override
    public List<ChannelConfigIndexParams> getThisReqParams() {
        Class<OrderReqParams> clazz = OrderReqParams.class;
        Field[] fields = clazz.getDeclaredFields();
        List<ChannelConfigIndexParams> list = new ArrayList<>();
        for (Field field : fields) {
            String name = field.getName();
            if (field.isAnnotationPresent(Remark.class)) {
                Remark remarkAnnotation = field.getAnnotation(Remark.class);
                ChannelConfigIndexParams indexParams = new ChannelConfigIndexParams();
                indexParams.setChineseStr(remarkAnnotation.name());
                indexParams.setDownReqStr(name);
                list.add(indexParams);
            }
        }
        return list;
    }

    @Override
    public void saveChannelConfig(Long channelId, ChannelConfigReqParams reqParams) {
        Optional<ChannelEntity> byId = channelRepository.findById(channelId);
        Assert.mustBeTrue(byId.isPresent(), "通道不存在:" + channelId);
        byId.get().getChannelReqParamsEntities().clear();

        List<ChannelConfigIndexParams> indexParams = reqParams.getIndexParams();
        List<ChannelReqParamsEntity> reqParamsEntities = BeanCopyUtils.copyList(indexParams, ChannelReqParamsEntity.class);

        byId.get().getChannelReqParamsEntities().addAll(reqParamsEntities);
        channelRepository.save(byId.get());
    }

    @Override
    public List<ChannelConfigIndexParams> getChannelConfig(Long id) {
        ChannelEntity channel = channelRepository.getOne(id);
        List<ChannelReqParamsEntity> reqParamsEntities = new ArrayList<>(channel.getChannelReqParamsEntities());
        return BeanCopyUtils.copyList(reqParamsEntities, ChannelConfigIndexParams.class);
    }

    @Autowired
    ChannelService channelService;

    @Autowired
    ChannelRepository channelRepository;

}
