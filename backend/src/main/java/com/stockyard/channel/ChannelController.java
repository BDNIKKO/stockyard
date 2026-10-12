package com.stockyard.channel;

import com.stockyard.auth.MakerRepository;
import com.stockyard.common.ApiException;
import com.stockyard.sale.SaleRepository;
import com.stockyard.security.MakerPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {
    public record ChannelRequest(@NotBlank @Size(max = 80) String name) {}

    public record ChannelResponse(Long id, String name) {
        static ChannelResponse of(Channel c) { return new ChannelResponse(c.getId(), c.getName()); }
    }

    private final ChannelRepository channels;
    private final MakerRepository makers;
    private final SaleRepository sales;

    public ChannelController(ChannelRepository channels, MakerRepository makers, SaleRepository sales) {
        this.channels = channels;
        this.makers = makers;
        this.sales = sales;
    }

    @GetMapping
    public List<ChannelResponse> list(@AuthenticationPrincipal MakerPrincipal me) {
        return channels.findByMakerIdOrderByNameAsc(me.id()).stream().map(ChannelResponse::of).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ChannelResponse create(@AuthenticationPrincipal MakerPrincipal me, @Valid @RequestBody ChannelRequest req) {
        String name = req.name().trim();
        if (channels.existsByMakerIdAndNameIgnoreCase(me.id(), name)) {
            throw ApiException.conflict("Channel \"" + name + "\" already exists");
        }
        return ChannelResponse.of(channels.save(new Channel(makers.getReferenceById(me.id()), name)));
    }

    @PutMapping("/{id}")
    @Transactional
    public ChannelResponse rename(@AuthenticationPrincipal MakerPrincipal me, @PathVariable Long id,
                                  @Valid @RequestBody ChannelRequest req) {
        Channel channel = channels.findByIdAndMakerId(id, me.id()).orElseThrow(() -> ApiException.notFound("Channel"));
        String name = req.name().trim();
        if (!channel.getName().equalsIgnoreCase(name) && channels.existsByMakerIdAndNameIgnoreCase(me.id(), name)) {
            throw ApiException.conflict("Channel \"" + name + "\" already exists");
        }
        channel.setName(name);
        return ChannelResponse.of(channel);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@AuthenticationPrincipal MakerPrincipal me, @PathVariable Long id) {
        Channel channel = channels.findByIdAndMakerId(id, me.id()).orElseThrow(() -> ApiException.notFound("Channel"));
        if (sales.existsByChannelId(id)) {
            throw ApiException.conflict("This channel has recorded sales and cannot be deleted");
        }
        channels.delete(channel);
    }
}
