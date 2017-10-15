package com.mackenziehigh.casecadefx.view.form;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * An instance of this class allows a developer to easily
 * create customized forms that look good out of the box,
 * but can be further styled using custom CSS.
 *
 * <p>
 * Logically, a form consists of a series of form-elements.
 * A form-element may simply be a control (e.g. text-field)
 * or more complex (e.g. a set of radio buttons).
 * </p>
 *
 * <p>
 * From an implementation standpoint, a form is a VBox.
 * Each row in the VBox is an HBox for a single form-element.
 * <p>
 */
public final class FormPane
        extends VBox
{
    /**
     * Super Type of All Form Elements.
     *
     * @param <T>
     */
    public class FormRow<T extends FormRow<T>>
            extends AnchorPane
    {
        public T setBackgroundColor (Color value)
        {
            return (T) this;
        }
    }

    public final class FormLabel
            extends FormRow<FormLabel>
    {
        private final Label label = new Label();

        private FormLabel ()
        {
            // Pass
        }

        public Label label ()
        {
            return label;
        }

        public FormLabel alignText (Pos value)
        {
            label.setAlignment(value);
            return this;
        }
    }

    public final class FormTextField
            extends FormRow<FormTextField>
    {
        private final TextField field = new TextField();

        private FormTextField ()
        {
            // Pass
        }

        public TextField field ()
        {
            return field;
        }
    }

    public final class FormTextArea
            extends FormRow<FormTextArea>
    {
        private final TextArea area = new TextArea();

        private FormTextArea ()
        {
            // Pass
        }

        public TextArea area ()
        {
            return area;
        }
    }

    public final class FormListBox
            extends FormRow<FormListBox>
    {
        private final ListView<String> listView = new ListView<>();

        private FormListBox ()
        {
            // Pass
        }

        public ListView<String> listView ()
        {
            return listView;
        }
    }

    public final class FormChoiceBox
            extends FormRow<FormChoiceBox>
    {
        private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

        private FormChoiceBox ()
        {
            // Pass
        }

        public ChoiceBox<String> choiceBox ()
        {
            return choiceBox;
        }
    }

    public final class FormRadioButton
            extends FormRow<FormRadioButton>
    {
        private final RadioButton button = new RadioButton();

        private FormRadioButton ()
        {
            // Pass
        }

        public RadioButton button ()
        {
            return button;
        }
    }

    public final class FormCheckBox
            extends FormRow<FormCheckBox>
    {
        private final CheckBox checkbox = new CheckBox();

        private FormCheckBox ()
        {
            // Pass
        }

        public CheckBox checkbox ()
        {
            return checkbox;
        }
    }

    public final class FormButtonBar
            extends FormRow<FormButtonBar>
    {

        private final List<Button> buttons = Lists.newLinkedList();

        private FormButtonBar ()
        {
            // Pass
        }

        public List<Button> buttons ()
        {
            return Collections.unmodifiableList(buttons);
        }
    }

    public final class FormDateTimePicker
    {

    }

    public final class FormColorPicker
    {

    }

    public final class FormDirectoryPicker
    {

    }

    public final class FormFilePicker
    {

    }

    public final class FormImageView
    {

    }

    /**
     * An (M x N) grid of image icons, which can be clicked.
     */
    public final class FormIconGrid
    {
        private final GridPane grid = new GridPane();

        private FormIconGrid ()
        {
            // Pass
        }

    }

    public final class FormTable
    {

    }

    public final class FormProgressBar
    {

    }

    public final class FormSlider
    {

    }

    public final class FormSliderTimeRange
    {

    }

    public final class FormSliderWithValues
    {

    }

    public final class FormSpinner
    {

    }

    public final class FormPasswordField
    {

    }

    public final class FormPagination
            extends FormRow<FormPagination>
    {
        private final Pagination pagination = new Pagination();

        private FormPagination ()
        {
            // Pass
        }

        public Pagination pagination ()
        {
            return pagination;
        }
    }

    public final class FormSeperator
            extends FormRow
    {

    }

    public final class FormLED
    {

    }

    public final class FormNode
    {

    }

    private final Map<String, FormRow> elements = new HashMap<>();

    private final Map<String, ToggleGroup> toggleGroups = new HashMap<>();

    public FormPane (final String name,
                     final String... css)
    {
        setId(name);
        getStylesheets().addAll(Arrays.asList(css));
    }

    public Map<String, FormRow> getFormElements ()
    {
        return Collections.unmodifiableMap(elements);
    }

    public FormPane setPadding (final double top,
                                final double right,
                                final double bottom,
                                final double left)
    {
        this.setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    public FormLabel addLabel (final String name)
    {
        final FormLabel element = new FormLabel();
        element.setId(name + "_row");
        element.getStylesheets().addAll(getStylesheets());
        element.label.setId(name);
        element.label.getStylesheets().addAll(getStylesheets());
        element.getChildren().add(element.label);
        elements.put(name, element);
        getChildren().add(element);
        AnchorPane.setLeftAnchor(element.label, 0.0);
        AnchorPane.setRightAnchor(element.label, 0.0);
        return element;
    }

    public FormTextField addTextField (final String name)
    {
        final FormTextField element = new FormTextField();
        element.setId(name + "_row");
        element.getStylesheets().addAll(getStylesheets());
        element.field.setId(name);
        element.field.getStylesheets().addAll(getStylesheets());
        element.getChildren().add(element.field);
        elements.put(name, element);
        getChildren().add(element);
        AnchorPane.setLeftAnchor(element.field, 0.0);
        AnchorPane.setRightAnchor(element.field, 0.0);
        return element;
    }

    public FormTextArea addTextArea (final String name)
    {
        final FormTextArea element = new FormTextArea();
        element.setId(name + "_row");
        element.getStylesheets().addAll(getStylesheets());
        element.area.setId(name);
        element.area.getStylesheets().addAll(getStylesheets());
        element.getChildren().add(element.area);
        elements.put(name, element);
        getChildren().add(element);
        AnchorPane.setLeftAnchor(element.area, 0.0);
        AnchorPane.setRightAnchor(element.area, 0.0);
        return element;
    }

    public FormListBox addListBox (final String name)
    {
        final FormListBox element = new FormListBox();
        element.setId(name + "_row");
        element.getStylesheets().addAll(getStylesheets());
        element.listView.setId(name);
        element.listView.getStylesheets().addAll(getStylesheets());
        element.getChildren().add(element.listView);
        elements.put(name, element);
        getChildren().add(element);
        AnchorPane.setLeftAnchor(element.listView, 0.0);
        AnchorPane.setRightAnchor(element.listView, 0.0);
        return element;
    }

    public FormChoiceBox addChoiceBox (final String name)
    {
        final FormChoiceBox element = new FormChoiceBox();
        element.setId(name + "_row");
        element.getStylesheets().addAll(getStylesheets());
        element.choiceBox.setId(name);
        element.choiceBox.getStylesheets().addAll(getStylesheets());
        element.getChildren().add(element.choiceBox);
        elements.put(name, element);
        getChildren().add(element);
        AnchorPane.setLeftAnchor(element.choiceBox, 0.0);
        AnchorPane.setRightAnchor(element.choiceBox, 0.0);
        return element;
    }

    public FormButtonBar addButtonBar ()
    {
        return null;
    }

    public FormPagination addPagination (final String name)
    {
        final FormPagination element = new FormPagination();
        element.setId(name + "_row");
        element.getStylesheets().addAll(getStylesheets());
        element.pagination.setId(name);
        element.pagination.getStylesheets().addAll(getStylesheets());
        element.getChildren().add(element.pagination);
        elements.put(name, element);
        getChildren().add(element);
        AnchorPane.setLeftAnchor(element.pagination, 0.0);
        AnchorPane.setRightAnchor(element.pagination, 0.0);
        return element;
    }

    public FormRadioButton addRadioButton (final String name,
                                           final String toggleGroupName)
    {
        if (toggleGroups.containsKey(toggleGroupName) == false)
        {
            toggleGroups.put(toggleGroupName, new ToggleGroup());
        }

        final FormRadioButton element = new FormRadioButton();
        element.setId(name + "_row");
        element.getStylesheets().addAll(getStylesheets());
        element.button.setId(name);
        element.button.setToggleGroup(toggleGroups.get(toggleGroupName));
        element.button.getStylesheets().addAll(getStylesheets());
        element.getChildren().add(element.button);
        AnchorPane.setLeftAnchor(element.button, 0.0);
        AnchorPane.setRightAnchor(element.button, 0.0);
        elements.put(name, element);
        getChildren().add(element);
        return element;
    }

    public FormCheckBox addCheckBox (final String name)
    {
        final FormCheckBox element = new FormCheckBox();
        element.setId(name + "_row");
        element.getStylesheets().addAll(getStylesheets());
        element.checkbox.setId(name);
        element.checkbox.getStylesheets().addAll(getStylesheets());
        element.getChildren().add(element.checkbox);
        AnchorPane.setLeftAnchor(element.checkbox, 0.0);
        AnchorPane.setRightAnchor(element.checkbox, 0.0);
        elements.put(name, element);
        getChildren().add(element);
        return element;
    }

}
