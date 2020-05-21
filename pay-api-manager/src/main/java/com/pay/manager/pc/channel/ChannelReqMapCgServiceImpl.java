package com.pay.manager.pc.channel;

import com.pay.common.annotation.Remark;
import com.pay.common.exception.Assert;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.ChannelReqMapEntity;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.mapper.ChannelReqParamsRepository;
import com.pay.data.params.OrderReqParams;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.channel.params.ChannelReqIndexParams;
import com.pay.manager.pc.channel.params.ChannelReqMapReqParams;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChannelReqMapCgServiceImpl extends AbstractHelper<ChannelReqParamsRepository, ChannelReqMapEntity, Long> implements ChannelReqMapCgService {

    @Override
    public List<ChannelReqIndexParams> getThisReqParams() {
        Class<OrderReqParams> clazz = OrderReqParams.class;
        Field[] fields = clazz.getDeclaredFields();
        List<ChannelReqIndexParams> list = new ArrayList<>();
        for (Field field : fields) {
            String name = field.getName();
            if (field.isAnnotationPresent(Remark.class)) {
                Remark remarkAnnotation = field.getAnnotation(Remark.class);
                ChannelReqIndexParams indexParams = new ChannelReqIndexParams();
                indexParams.setChineseStr(remarkAnnotation.name());
                indexParams.setDownReqStr(name);
                list.add(indexParams);
            }
        }
        return list;
    }

    @Override
    public void saveChannelConfig(Long channelId, ChannelReqMapReqParams reqParams) {
        Optional<ChannelEntity> byId = channelRepository.findById(channelId);
        Assert.mustBeTrue(byId.isPresent(), "通道不存在:" + channelId);
        byId.get().getChannelReqMapEntities().clear();

        List<ChannelReqIndexParams> indexParams = reqParams.getIndexParams();
        List<ChannelReqMapEntity> reqParamsEntities = BeanCopyUtils.copyList(indexParams, ChannelReqMapEntity.class);

        byId.get().getChannelReqMapEntities().addAll(reqParamsEntities);
        channelRepository.save(byId.get());
    }

    @Override
    public List<ChannelReqIndexParams> getChannelConfig(Long id) {
        ChannelEntity channel = channelRepository.getOne(id);
        List<ChannelReqMapEntity> reqParamsEntities = new ArrayList<>(channel.getChannelReqMapEntities());
        return BeanCopyUtils.copyList(reqParamsEntities, ChannelReqIndexParams.class);
    }

    @Autowired
    ChannelService channelService;

    @Autowired
    ChannelRepository channelRepository;

}
