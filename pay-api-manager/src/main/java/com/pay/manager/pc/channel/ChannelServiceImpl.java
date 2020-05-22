package com.pay.manager.pc.channel;

import com.pay.common.constant.Constant;
import com.pay.common.page.PageReqParams;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.channel.params.*;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChannelServiceImpl extends AbstractHelper<ChannelRepository, ChannelEntity, Long> implements ChannelService {

    @Override
    public Long insert(ChannelReqParams reqParams) {
        //保存通道基本信息
        return save(BeanCopyUtils.copyBean(reqParams, ChannelEntity.class, ChannelBeanOption.class)).getId();
    }

    @Override
    public void update(ChannelReqParams reqParams, Long id) {
        ChannelEntity channel = getById(id);
        save(BeanCopyUtils.copyBean(reqParams, channel, ChannelBeanOption.class));
    }

    @Override
    public void delete(Long id) {
        deleteById(id);
    }

    @Override
    public ChannelDetailRespParams select(Long id) {
        return BeanCopyUtils.copyBean(getById(id), ChannelDetailRespParams.class);
    }

    @Override
    public Page<ChannelPageRespParams> selectPage(ChannelQuery query, PageReqParams reqParams) {
        Page<ChannelEntity> list = getPage(query, toPageable(reqParams, Constant.CREATE_TIME));
        return list.map(e -> pageCopy(e, ChannelPageRespParams.class));
    }
}
