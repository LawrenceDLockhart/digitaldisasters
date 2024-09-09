package com.example.digitaldisasters.views;

import com.example.digitaldisasters.services.S3MemoryService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.HashSet;
import java.util.stream.Collectors;

@Route("")
public class UploadView extends VerticalLayout {

    private final S3MemoryService service;
    private final VerticalLayout memoriesList = new VerticalLayout();

    public UploadView(S3MemoryService service) {
        this.service = service;
        StreamResource imageResource = new StreamResource("webdev1.jpg",
                () -> getClass().getResourceAsStream("/webdev1.jpg"));
        Image logo = new Image(imageResource, "Digital Disasters logo");
        logo.setMaxWidth("300px");
        HorizontalLayout logoLayout = new HorizontalLayout(logo);
        setHeightFull();
        setMaxWidth("500px");
        addClassNames(LumoUtility.Margin.Horizontal.AUTO);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        H1 title = new H1("Digital Disasters");
        title.getStyle().set("font-family", "Roboto, sans-serif");
        title.getStyle().set("font-size", "3rem");
        add(logoLayout, title, getForm());
        addAndExpand(new Scroller(memoriesList));
        updateMemories("");
    }

    private HorizontalLayout getForm() {
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
        tagInput.addValueChangeListener(e -> {
            Notification.show(
                    e.getValue().stream().collect(Collectors.joining(",")));
        });

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.addSucceededListener(event -> {
            String description = descripInput.getValue();
            var selectedTags = tagInput.getValue();
            service.saveMemory(buffer.getInputStream(), event.getFileName(), description, selectedTags);
            updateMemories("");
            descripInput.clear();
            tagInput.clear();
            upload.clearFileList();
        });

        var tagsAndUploadLayout = new VerticalLayout(tagInput, upload);
        tagsAndUploadLayout.setWidth(null);
        tagsAndUploadLayout.setPadding(false);
        tagInput.setWidthFull();
        return new HorizontalLayout(descripInput, tagsAndUploadLayout);
    }

    private void updateMemories(String tag) {
        memoriesList.removeAll();
        service.findByTag(tag).forEach(mem -> {
            var img = new Image(mem.getImageUrl(), "Uploaded image");
            img.setWidth("400px");
            img.addClickListener(e -> {
                UI.getCurrent().navigate(ImageView.class, mem.getId());
            });

            Paragraph descriptionParagraph = new Paragraph(mem.getDescription());

            var tagLayout = new HorizontalLayout();
            mem.getTags().forEach(t -> {
                Button tagButton = new Button(t, e -> updateMemories(t));
                tagButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                tagLayout.add(tagButton);
            });

            Button deleteButton = new Button("DELETE");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.addClickListener(clickEvent -> {
                service.deleteMemory(mem);
                updateMemories(tag);
                Notification.show("Memory deleted!");
            });
            VerticalLayout memoryLayout = new VerticalLayout(img, tagLayout, descriptionParagraph, deleteButton);
            memoryLayout.addClassNames(
                    LumoUtility.Margin.Bottom.LARGE,
                    LumoUtility.Border.BOTTOM
            );
            memoriesList.add(memoryLayout);
        });
    }


}