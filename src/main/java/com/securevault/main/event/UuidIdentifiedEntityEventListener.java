package com.securevault.main.event;

import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.lang.NonNull;

import com.securevault.main.entity.AbstractUuidIdentified;

public class UuidIdentifiedEntityEventListener extends AbstractMongoEventListener<AbstractUuidIdentified> {

	public void onBeforeConvert(@NonNull BeforeConvertEvent<AbstractUuidIdentified> event) {
		super.onBeforeConvert(event);

		AbstractUuidIdentified entity = event.getSource();

		if (entity.getId() == null) {
			entity.setId(UUID.randomUUID());
		}

	}

}
