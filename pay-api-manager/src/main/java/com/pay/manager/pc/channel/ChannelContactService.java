package com.pay.manager.pc.channel;

import com.pay.manager.pc.contact.ContactParams;

import java.util.List;

public interface ChannelContactService {

    void insertOrUpdate(Long channelId, List<ContactParams> contactParams);

    void delete(Long channelId);

    List<ContactParams> getChannelContactList(Long channelId);
}
