package dev.simplix.protocolize.velocity.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.querz.nbt.tag.*;

import java.lang.reflect.ParameterizedType;
import java.util.*;

public class TagUtil {

    public static Tag<?> fromJson(JsonElement json) {
        if (json instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive)json;
            if (!jsonPrimitive.isNumber()) {
                if (jsonPrimitive.isString()) {
                    return new StringTag(jsonPrimitive.getAsString());
                }

                if (jsonPrimitive.isBoolean()) {
                    return new ByteTag((byte) (jsonPrimitive.getAsBoolean() ? 1 : 0));
                }

                throw new IllegalArgumentException("Unknown JSON primitive: " + jsonPrimitive);
            }

            Number number = json.getAsNumber();
            if (number instanceof Byte) {
                return new ByteTag((byte)number);
            }

            if (number instanceof Short) {
                return new ShortTag((short)number);
            }

            if (number instanceof Integer) {
                return new IntTag((int)number);
            }

            if (number instanceof Long) {
                return new LongTag((long)number);
            }

            if (number instanceof Float) {
                return new FloatTag((float)number);
            }

            if (number instanceof Double) {
                return new DoubleTag((double)number);
            }
        } else {
            if (json instanceof JsonObject) {
                CompoundTag compoundTag = new CompoundTag();
                Iterator iterator = ((JsonObject)json).entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<String, JsonElement> property = (Map.Entry)iterator.next();
                    compoundTag.put(property.getKey(), fromJson(property.getValue()));
                }

                return compoundTag;
            }

            if (json instanceof JsonArray) {
                List<JsonElement> jsonArray = ((JsonArray)json).asList();

                if (jsonArray.isEmpty()) {
                    return new ListTag(IntTag.class);
                }

                Class<?> clazz = TypeToken.of(((ParameterizedType) fromJson(jsonArray.get(0)).getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getRawType();
                Tag<?> listTag;

                if (clazz == Byte.class) {
                    byte[] bytes = new byte[jsonArray.size()];

                    for (int i = 0; i < bytes.length; ++i) {
                        bytes[i] = (Byte) (jsonArray.get(i)).getAsNumber();
                    }

                    listTag = new ByteArrayTag(bytes);
                } else if (clazz == Integer.class) {
                    int[] ints = new int[jsonArray.size()];

                    for (int i = 0; i < ints.length; ++i) {
                        ints[i] = (Integer) (jsonArray.get(i)).getAsNumber();
                    }

                    listTag = new IntArrayTag(ints);
                } else if (clazz == Long.class) {
                    long[] longs = new long[jsonArray.size()];

                    for(int i = 0; i < longs.length; ++i) {
                        longs[i] = (Long)(jsonArray.get(i)).getAsNumber();
                    }

                    listTag = new LongArrayTag(longs);
                } else {
                    ListTag<CompoundTag> tagItems = new ListTag(CompoundTag.class);

                    Tag<?> subTag;
                    for(Iterator var8 = jsonArray.iterator(); var8.hasNext(); tagItems.add((CompoundTag) subTag)) {
                        JsonElement jsonEl = (JsonElement)var8.next();
                        subTag = fromJson(jsonEl);
                        if (!(subTag instanceof CompoundTag)) {
                            CompoundTag wrapper = new CompoundTag();
                            wrapper.put("", subTag);
                            subTag = wrapper;
                        }
                    }

                    listTag = tagItems;
                }

                return listTag;
            }

            if (json instanceof JsonNull) {
                return EndTag.INSTANCE;
            }
        }

        throw new IllegalArgumentException("Unknown JSON element: " + json);
    }
}
