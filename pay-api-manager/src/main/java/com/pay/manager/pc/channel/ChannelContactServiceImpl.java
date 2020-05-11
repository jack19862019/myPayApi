package com.pay.manager.pc.channel;

import com.pay.common.exception.Assert;
import com.pay.data.entity.ChannelContactEntity;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.mapper.ChannelContactRepository;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.contact.ContactParams;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChannelContactServiceImpl extends AbstractHelper<ChannelContactRepository, ChannelContactEntity, Long> implements ChannelContactService {

    @Override
    public void insertOrUpdate(Long channelId, List<ContactParams> contactParams) {
        Optional<ChannelEntity> byId = channelRepository.findById(channelId);
        Assert.mustBeTrue(byId.isPresent(), "通道不存在:" + channelId);
        byId.get().getContacts().clear();
        List<ChannelContactEntity> contactEntityList = BeanCopyUtils.copyList(contactParams, ChannelContactEntity.class);
        byId.get().getContacts().addAll(contactEntityList);
        channelRepository.save(byId.get());
    }

    @Override
    public void delete(Long channelId) {
        channelContactRepository.deleteAllByChannel_Id(channelId);
    }

    @Override
    public List<ContactParams> getChannelContactList(Long channelId) {
        List<ChannelContactEntity> contactEntities = channelContactRepository.findByChannel_Id(channelId);
        return BeanCopyUtils.copyList(contactEntities, ContactParams.class);
    }

    @Autowired
    ChannelContactRepository channelContactRepository;

    @Autowired
    ChannelRepository channelRepository;
}
