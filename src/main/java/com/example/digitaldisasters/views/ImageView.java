package com.example.digitaldisasters.views;

import com.example.digitaldisasters.model.Memory;
import com.example.digitaldisasters.services.S3MemoryService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

@Route("image")
public class ImageView extends VerticalLayout implements HasUrlParameter<Long> {

    private final S3MemoryService service;

    public ImageView(S3MemoryService service) {
        this.service = service;
    }

    @Override
    public void setParameter(BeforeEvent event, Long memoryId) {
        Div singleImage = new Div();
        singleImage.addClassName("single-image");
        Memory memory = service.findById(memoryId);
        Image image = new Image(memory.getImageUrl(), "Uploaded image");
        image.setWidth("600px");
        // Change to textfield | create binder, update (need new update method in service), navigate to upload ivew
        Paragraph description = new Paragraph(memory.getDescription());
        Paragraph tags = new Paragraph(String.join(", ", memory.getTags()));
        Button button = new Button("Home Page");
        button.addClickListener(e -> {
            UI.getCurrent().navigate("");
        });
        singleImage.add(image, description, tags, button);
        add(singleImage);
    }
}
