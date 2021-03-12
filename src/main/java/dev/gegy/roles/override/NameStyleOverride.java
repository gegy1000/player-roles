package dev.gegy.roles.override;

import com.mojang.serialization.Codec;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.ArrayList;
import java.util.List;

public final class NameStyleOverride {
    public static final Codec<NameStyleOverride> CODEC = MoreCodecs.listOrUnit(Codec.STRING).xmap(
            formatKeys -> {
                List<Formatting> formats = new ArrayList<>();
                TextColor color = null;

                for (String formatKey : formatKeys) {
                    Formatting format = Formatting.byName(formatKey);
                    if (format != null) {
                        formats.add(format);
                    } else {
                        TextColor parsedColor = TextColor.parse(formatKey);
                        if (parsedColor != null) {
                            color = parsedColor;
                        }
                    }
                }

                return new NameStyleOverride(formats.toArray(new Formatting[0]), color);
            },
            override -> {
                List<String> formatKeys = new ArrayList<>();
                if (override.color != null) {
                    formatKeys.add(override.color.getName());
                }

                for (Formatting format : override.formats) {
                    formatKeys.add(format.getName());
                }

                return formatKeys;
            }
    );

    private final Formatting[] formats;
    private final TextColor color;

    public NameStyleOverride(Formatting[] formats, @Nullable TextColor color) {
        this.formats = formats;
        this.color = color;
    }

    public MutableText apply(MutableText text) {
        return text.setStyle(this.applyStyle(text.getStyle()));
    }

    private Style applyStyle(Style style) {
        style = style.withFormatting(this.formats);
        if (this.color != null) {
            style = style.withColor(this.color);
        }
        return style;
    }
}
