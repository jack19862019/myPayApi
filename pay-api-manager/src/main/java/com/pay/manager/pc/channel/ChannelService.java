package com.pay.manager.pc.channel;

import com.pay.common.page.PageReqParams;
import com.pay.data.entity.ChannelEntity;
import com.pay.manager.pc.channel.params.ChannelDetailRespParams;
import com.pay.manager.pc.channel.params.ChannelPageRespParams;
import com.pay.manager.pc.channel.params.ChannelQuery;
import com.pay.manager.pc.channel.params.ChannelReqParams;
import org.springframework.data.domain.Page;

public interface ChannelService {

    void insert(ChannelReqParams reqParams);

    void update(ChannelReqParams reqParams, Long id);

    void delete(Long id);

    ChannelDetailRespParams select(Long id);

    Page<ChannelPageRespParams> selectPage(ChannelQuery query, PageReqParams reqParams);
}
