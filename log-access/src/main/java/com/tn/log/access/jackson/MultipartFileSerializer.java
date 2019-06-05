package com.tn.log.access.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 *
 * @author chenck
 * @date 2019/5/10 15:49
 */
public class MultipartFileSerializer extends StdScalarSerializer<MultipartFile> {
    public MultipartFileSerializer() {
        super(MultipartFile.class);
    }

    @Override
    public void serialize(MultipartFile value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();
        gen.writeStringField("__filename", value.getName());
        gen.writeStringField("__org_filename", value.getOriginalFilename());
        gen.writeNumberField("__f_size", value.getSize());
        gen.writeEndObject();
    }
}
