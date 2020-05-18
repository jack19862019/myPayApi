package com.pay.data.entity;

import com.pay.common.enums.EncryptionType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Where(clause = "is_delete=1")
@Table(name = "mcp_config")
public class McpConfigEntity extends BaseEntity {

    //MD5就是单个的值，RSA就是json
    @Column(columnDefinition = "text")
    private String upKey;

    private String upMerchantNo;

    @Convert(converter = EncryptionType.Convert.class)
    private EncryptionType encryptionType;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private ChannelEntity channel;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private MerchantEntity merchant;

    /*    @OneToMany(cascade = CascadeType.ALL,mappedBy = "mcpConfig", orphanRemoval = true)
    private Set<McpPayTypeEntity> mcpPayType = new HashSet<>();*/
}
