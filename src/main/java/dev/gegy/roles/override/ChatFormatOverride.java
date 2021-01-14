package dev.gegy.roles.override;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatFormatOverride {
    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    private static final Object NAME_MARKER = new Object();
    private static final Object CONTENT_MARKER = new Object();

    private final Object[] format;
    private final Builder builder = new Builder();

    public ChatFormatOverride(String format) {
        this.format = parseFormat(format);
    }

    private static Object[] parseFormat(String formatString) {
        Parser parser = new Parser();

        int lastIdx = 0;
        int currentArgumentIdx = 0;

        Matcher argumentMatcher = ARGUMENT_PATTERN.matcher(formatString);
        while (argumentMatcher.find(lastIdx)) {
            int argumentStart = argumentMatcher.start();
            int argumentEnd = argumentMatcher.end();

            if (argumentStart > lastIdx) {
                parser.add(formatString.substring(lastIdx, argumentStart));
            }

            String index = argumentMatcher.group(1);
            String type = argumentMatcher.group(2);
            if (type.equals("s")) {
                int argumentIdx;
                if ("1".equals(index)) {
                    argumentIdx = 0;
                } else if ("2".equals(index)) {
                    argumentIdx = 1;
                } else {
                    argumentIdx = currentArgumentIdx++;
                }
                if (argumentIdx == 0) {
                    parser.markName();
                } else if (argumentIdx == 1) {
                    parser.markContent();
                } else {
                    parser.add(formatString.substring(argumentStart, argumentEnd));
                }
            }

            lastIdx = argumentEnd;
        }

        if (lastIdx < formatString.length()) {
            parser.add(formatString.substring(lastIdx));
        }

        return parser.getFormat();
    }

    public Text make(Text name, String content) {
        Builder builder = this.builder;
        for (Object format : this.format) {
            if (format == NAME_MARKER) {
                builder.pushText(name);
            } else if (format == CONTENT_MARKER) {
                builder.pushString(content);
            } else {
                builder.pushString((String) format);
            }
        }
        return builder.get();
    }

    static final class Builder {
        private final StringBuilder builder = new StringBuilder();
        private MutableText result;

        void pushText(Text text) {
            this.flushStringBuilder();
            this.appendText(text.shallowCopy());
        }

        void pushString(String string) {
            this.builder.append(string);
        }

        private void flushStringBuilder() {
            StringBuilder builder = this.builder;
            if (builder.length() > 0) {
                MutableText text = new LiteralText(builder.toString());
                builder.setLength(0);
                this.appendText(text);
            }
        }

        private void appendText(MutableText text) {
            if (this.result == null) {
                this.result = text;
            } else {
                this.result = this.result.append(text);
            }
        }

        MutableText get() {
            this.flushStringBuilder();

            MutableText result = this.result;
            this.result = null;
            this.builder.setLength(0);
            return result;
        }
    }

    static final class Parser {
        private final List<Object> format = new ArrayList<>();

        void add(String text) {
            this.format.add(text);
        }

        void markName() {
            this.format.add(NAME_MARKER);
        }

        void markContent() {
            this.format.add(CONTENT_MARKER);
        }

        Object[] getFormat() {
            return this.format.toArray(new Object[0]);
        }
    }
}
