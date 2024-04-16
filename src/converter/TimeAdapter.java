package converter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import exception.ParsingException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeAdapter extends TypeAdapter<LocalDateTime> {

    public static final DateTimeFormatter DATE_TIME_FORMAT_1 = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final DateTimeFormatter TIME_FORMAT_1 = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        if (localDateTime == null) {
            jsonWriter.value("null");
            return;
        }
        jsonWriter.value(localDateTime.format(DATE_TIME_FORMAT_1));
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        final String string = jsonReader.nextString();
        try {
            return LocalDateTime.parse(string, DATE_TIME_FORMAT_1);
        } catch (DateTimeParseException e) {
            throw new ParsingException("Неверный формат даты: " + string + " необходим dd.MM.yyyy HH:mm");
        }
    }
}
