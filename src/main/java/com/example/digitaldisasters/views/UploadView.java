package com.example.digitaldisasters.views;

import com.example.digitaldisasters.services.S3MemoryService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;

import java.util.HashSet;
import java.util.stream.Collectors;

@Route("")
public class UploadView extends VerticalLayout {

    private final S3MemoryService service;
    private final FlexLayout memoriesList = new FlexLayout();

    public UploadView(S3MemoryService service) {
        this.service = service;

        Image logo = new Image("main/resources/images/webdev1.jpg", "Digital Disasters logo");
        H1 h1 = new H1("Digital Disasters");
        h1.addClassName("lumo-center");
        H3 h3 = new H3("My Dev Diary");
        memoriesList.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        MemoryBuffer buffer = new MemoryBuffer();

        TextArea descripInput = new TextArea("Description");
        MultiSelectComboBox<String> tagInput = new MultiSelectComboBox<>("Tags");
        var tags = tagInput.setItems("missing punctuation", "git issues", "off by one", "test in prod", "works on my machine",
                "endless loop");
        tagInput.setAllowCustomValue(true);
        tagInput.addCustomValueSetListener(e -> {
            var newTag = e.getDetail();
            tags.addItem(newTag);
            var selected = new HashSet<>(tagInput.getValue());
            selected.add(newTag);
            tagInput.setValue(selected);
        });
        add(tagInput);
        tagInput.addValueChangeListener(e -> {
            Notification.show(
                    e.getValue().stream().collect(Collectors.joining(",")));
        });
        Upload upload = new Upload(buffer);
        upload.addSucceededListener(event -> {
            String description = descripInput.getValue();
            var selectedTags = tagInput.getValue();
            service.saveMemory(buffer.getInputStream(), event.getFileName(), description, selectedTags);
            updateMemories("");
            descripInput.clear();
            tagInput.clear();
        });

        HorizontalLayout uploadComponents = new HorizontalLayout(descripInput, tagInput, upload);
        uploadComponents.addClassName("lumo-center");

        add(logo, h1, h3, uploadComponents, memoriesList);
        setSpacing(true);

        updateMemories("");
    }

    private void updateMemories(String tag) {
        memoriesList.removeAll();
        service.findByTag(tag).forEach(mem -> {
            var img = new Image(mem.getImageUrl(), "Uploaded image");
            img.setWidth("300px");
            img.addClickListener(e -> {
                UI.getCurrent().navigate(ImageView.class, mem.getId());
            });

            Paragraph descriptionParagraph = new Paragraph(mem.getDescription());

            var tagLayout = new HorizontalLayout();
            mem.getTags().forEach(t -> tagLayout.add(new Button(t, e -> updateMemories(t))));
            tagLayout.addClassName("lumo-endgroup");


            VerticalLayout memoryLayout = new VerticalLayout(img, descriptionParagraph, tagLayout);
            memoriesList.add(memoryLayout);
        });
    }
}