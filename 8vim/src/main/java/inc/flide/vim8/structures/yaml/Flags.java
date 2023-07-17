package inc.flide.vim8.structures.yaml;

import android.view.KeyEvent;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public abstract class Flags {

    public static Flags empty() {
        return new IntFlags(0);

    }

    public int getValue() {
        return 0;
    }

    public static class FlagsDeserializer extends JsonDeserializer<Flags> {

        @Override
        public Flags deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            JsonNode node = (JsonNode) p.getCodec().readTree(p);
            if (node.isInt()) {
                return new IntFlags(node.intValue());
            } else if (node.isTextual()) {
                return new IntFlags(getArrayFlag(node, p));
            } else if (node.isArray()) {
                ArrayNode arrayNode = (ArrayNode) node;
                Iterator<JsonNode> iterator = arrayNode.elements();
                if (!iterator.hasNext()) {
                    return Flags.empty();
                }
                List<Integer> flags = new ArrayList<>();

                while (iterator.hasNext()) {
                    JsonNode current = iterator.next();
                    flags.add(getArrayFlag(current, p));
                }
                return new ArrayFlags(flags);
            }

            throw MismatchedInputException.from(p, (Class<?>) null,
                    "When using an array for flags, it only support integer or string");

        }

        private int getArrayFlag(JsonNode node, JsonParser p) throws JacksonException {
            if (node.isInt() && node.intValue() >= 0) {
                return node.intValue();
            } else if (node.isInt()) {
                throw MismatchedInputException.from(p, (Class<?>) null, "flag value must be positive");
            } else if (node.isTextual()) {
                switch (node.textValue().toUpperCase(Locale.ROOT)) {
                    case "META_SHIFT_ON":
                        return KeyEvent.META_SHIFT_ON;
                    case "META_ALT_ON":
                        return KeyEvent.META_ALT_ON;
                    case "META_SYM_ON":
                        return KeyEvent.META_SYM_ON;
                    case "META_FUNCTION_ON":
                        return KeyEvent.META_FUNCTION_ON;
                    case "META_ALT_LEFT_ON":
                        return KeyEvent.META_ALT_LEFT_ON;
                    case "META_ALT_RIGHT_ON":
                        return KeyEvent.META_ALT_RIGHT_ON;
                    case "META_SHIFT_LEFT_ON":
                        return KeyEvent.META_SHIFT_LEFT_ON;
                    case "META_SHIFT_RIGHT_ON":
                        return KeyEvent.META_SHIFT_RIGHT_ON;
                    case "META_CTRL_ON":
                        return KeyEvent.META_CTRL_ON;
                    case "META_CTRL_LEFT_ON":
                        return KeyEvent.META_CTRL_LEFT_ON;
                    case "META_CTRL_RIGHT_ON":
                        return KeyEvent.META_CTRL_RIGHT_ON;
                    case "META_META_ON":
                        return KeyEvent.META_META_ON;
                    case "META_META_LEFT_ON":
                        return KeyEvent.META_META_LEFT_ON;
                    case "META_META_RIGHT_ON":
                        return KeyEvent.META_META_RIGHT_ON;
                    case "META_CAPS_LOCK_ON":
                        return KeyEvent.META_CAPS_LOCK_ON;
                    case "META_NUM_LOCK_ON":
                        return KeyEvent.META_NUM_LOCK_ON;
                    case "META_SCROLL_LOCK_ON":
                        return KeyEvent.META_SCROLL_LOCK_ON;
                    default:
                        throw MismatchedInputException.from(p, (Class<?>) null, "unknown meta modifier");
                }
            } else {
                throw MismatchedInputException.from(p, (Class<?>) null,
                        "When using an array for flags, all values must be of the same type");
            }
        }
    }

    public static class IntFlags extends Flags {
        private final int value;

        public IntFlags(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public static class ArrayFlags extends Flags {
        public List<Integer> flags;

        public ArrayFlags(List<Integer> flags) {
            this.flags = flags;
        }

        @Override
        public int getValue() {
            int value = 0;
            for (Integer flag : flags) {
                value |= flag;
            }
            return value;
        }
    }

}