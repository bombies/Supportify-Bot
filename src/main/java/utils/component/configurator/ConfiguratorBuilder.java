package utils.component.configurator;

import net.dv8tion.jda.api.entities.MessageEmbed;
import utils.component.InvalidBuilderException;

import java.util.ArrayList;
import java.util.List;

public class ConfiguratorBuilder {
    private final List<ConfiguratorOption> configuratorOptions;
    private MessageEmbed embed;

    public ConfiguratorBuilder() {
        this.configuratorOptions = new ArrayList<>();
    }

    public ConfiguratorBuilder addOption(ConfiguratorOption option) {
        configuratorOptions.add(option);
        return this;
    }

    public ConfiguratorBuilder setEmbed(MessageEmbed embed) {
        this.embed = embed;
        return this;
    }

    public AbstractConfigurator build() throws InvalidBuilderException {
        if (configuratorOptions.isEmpty())
            throw new InvalidBuilderException("The configuration options can't be empty!");
        if (embed == null)
            throw new InvalidBuilderException("The embed can't be null!");
        return new AbstractConfigurator(configuratorOptions, embed);
    }
}
