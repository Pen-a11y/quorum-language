function plugins_quorum_Libraries_Interface_Accessibility_WebAccessibility_() {
    /*
     * These variables are old and may not be relevant. I'm leaving them in for now, 
     * but presume they will be modified or go away as we develop the library.
     */
    var type = "";      //specifies the input type of the element
    var elementType = "DIV";   //specifies the type of element DEFAULT is DIV right now for testing
    var elementList = [];   // array using the item's hashCode value as an index and the item as the value 
    var currentFocus = null;
    
//    system action NameChanged(Item item)

    this.NameChanged$quorum_Libraries_Interface_Item = function(item) {
        var id = item.GetHashCode();
        if( elementList[id] != null ) {
            var element = document.getElementById(id);
            element.setAttribute("aria-label", item.GetName());
        }
        console.log("Name Changed");
    };

//    system action DescriptionChanged(Item item)

    this.DescriptionChanged$quorum_Libraries_Interface_Item = function(item) {
        var id = item.GetHashCode();
        if( elementList[id] != null ) {
            var element = document.getElementById(id);
            element.setAttribute("aria-roledescription", item.GetDescription());
        }
        console.log("Description Changed");
    };
    
//    system action TextFieldUpdatePassword(TextField field)

    this.TextFieldUpdatePassword$quorum_Libraries_Interface_Controls_TextField = function(field) {
        console.log("Text field updated Changed");
    };
    
//    system action Update
//this is handled in Quorum for now might be added back if need be
    this.Update = function() {
        //removed
        //console.log("Update called");
    };
    
//    system action ProgressBarValueChanged(ProgressBarValueChangedEvent progress)

    this.ProgressBarValueChanged$quorum_Libraries_Interface_Events_ProgressBarValueChangedEvent = function(event) {
        console.log("Progress bar updated");
    };
    
//  private system action TextSelectionChanged(TextBoxSelection selection)
    this.TextSelectionChanged$quorum_Libraries_Interface_Selections_TextBoxSelection = function(selection) {
        var textbox = selection.GetTextBox();
        if (textbox == null){
            return;
        }
        
    }

//  private system action TextSelectionChanged(TextBoxSelection selection)
    this.TextSelectionChanged$quorum_Libraries_Interface_Selections_TextFieldSelection = function(selection) {
        var textField = selection.GetTextField();
        if (textField == null){
            return;
        }
        
    }
    
//    system action SelectionChanged(SelectionEvent event)
    this.SelectionChanged$quorum_Libraries_Interface_Events_SelectionEvent = function(event) {
        
        var selection = event.GetSelection();
        if ( global_InstanceOf(selection,"Libraries.Interface.Selections.TextBoxSelection") )
        {
            selection = global_CheckCast(selection, "Libraries.Interface.Selections.TextBoxSelection");
            this.TextSelectionChanged$quorum_Libraries_Interface_Selections_TextBoxSelection(selection);
            console.log("TextBox Selection Changed");
        }
        else if ( global_InstanceOf(selection,"Libraries.Interface.Selections.TextFieldSelection") )
        {
            selection = global_CheckCast(selection, "Libraries.Interface.Selections.TextFieldSelection");
            var textField = selection.GetTextField();
            var id = textField.GetHashCode();
            var liveElement = document.getElementById(id+"-selection");
            liveElement.innerHTML = selection.GetText();
            console.log("TextField Selection Changed");
        }
        else if ( global_InstanceOf(selection,"Libraries.Interface.Selections.TabPaneSelection") )
        {
            selection = global_CheckCast(selection, "Libraries.Interface.Selections.TabPaneSelection");
            console.log("TabPane Selection Changed");
        }
        else if ( global_InstanceOf(selection,"Libraries.Interface.Selections.MenuSelection") )
        {
            selection = global_CheckCast(selection, "Libraries.Interface.Selections.MenuSelection");
            console.log("Menu Selection Changed");
        }
        else if ( global_InstanceOf(selection,"Libraries.Interface.Selections.TreeSelection") )
        {
            selection = global_CheckCast(selection, "Libraries.Interface.Selections.TreeSelection");
            console.log("Tree Selection Changed");
        }
        else if ( global_InstanceOf(selection,"Libraries.Interface.Selections.SpreadsheetSelection") )
        {
            selection = global_CheckCast(selection, "Libraries.Interface.Selections.SpreadsheetSelection");
            console.log("Spreadsheet Selection Changed");
        }
        else if ( global_InstanceOf(selection,"Libraries.Interface.Selections.ListSelection") )
        {
            selection = global_CheckCast(selection, "Libraries.Interface.Selections.ListSelection");
            console.log("List Selection Changed");
        }
        else if ( global_InstanceOf(selection,"Libraries.Interface.Selections.TreeTableSelection") )
        {
            selection = global_CheckCast(selection, "Libraries.Interface.Selections.TreeTableSelection");
            console.log("TreeTable Selection Changed");
        }
        else if ( global_InstanceOf(selection,"Libraries.Interface.Selections.ButtonGroupSelection") )
        {
            selection = global_CheckCast(selection, "Libraries.Interface.Selections.ButtonGroupSelection");
            console.log("ButtonGroup Selection Changed");
        }
        else {
            console.log("Selection Changed");
        }
        
    };
    
//    system action ButtonActivated(Button button)
    this.ButtonActivated$quorum_Libraries_Interface_Controls_Button = function(button) {
        console.log("Button Activated");
    };
    
//    system action ToggleButtonToggled(ToggleButton button)    
    this.ToggleButtonToggled$quorum_Libraries_Interface_Controls_ToggleButton = function(button) {
        console.log("Toggled Buttoned");
    };
    
//    system action FocusChanged(FocusEvent event)
    this.FocusChanged$quorum_Libraries_Interface_Events_FocusEvent = function(event) {
        console.log("Focus Changed");
        var item = event.GetNewFocus();
        var id = item.GetHashCode();
        currentFocus = item;
        var element = document.getElementById(currentIDECanvas_$Global_);
        element.setAttribute("aria-activedescendant", id)
    };
//    system action NativeAdd(Item item)
    this.NativeAdd$quorum_Libraries_Interface_Item = function(item) {
        //dont add to DOM if not accessible
        if (item.GetAccessibilityCode() == -1) {
            return;
        }
        
       var canvas = document.getElementById(currentIDECanvas_$Global_);

       //replace this code with item appropriate material
        var id = item.GetHashCode();
        //dont want to add something to the DOM twice
        if( elementList[id] != null ) {
            return;
        }
        var itemName = item.GetName();   //used for testing purposes
        elementList[id] = item;      //adds the item to the elementList array using the item's HashCode value as an index
        elementType = "DIV";
        //default role
        let role = "region";

        /* Creating Item Element Tag with Attributes */
        var para = document.createElement(elementType);
        para.id = id;       //sets the item's id to the item's HashCode value

        switch(item.GetAccessibilityCode()){
            //CUSTOM
            case 1:
                para.setAttribute("aria-roledescription","custom");
                break;
            //CHECKBOX
            case 2:
                role = "checkbox";
                //check for checked status
                break;
            //RADIO_BUTTON
            case 3:
                role = "radio";
                break;
            //BUTTON
            case 4:
                role = "button";
                break;
            //TOGGLE_BUTTON
            case 5:
                role = "button";
                //check for pressed
                break;
            //TEXTBOX
            case 6:
                role = "textbox";
                break;
            //MENU_BAR
            case 7:
                role = "menubar";
                break;
            //MENU_ITEM
            case 8:
                role = "menuitem";
                break;
            //PANE
            case 9:
                break;
            //TREE
            case 10:
                role = "tree";
                break;
            //TREE_ITEM
            case 11:
                role = "treeitem";
                break;
            //TOOLBAR
            case 12:
                role = "toolbar";
                break;
            //TAB
            case 13:
                role = "tab";
                break;
            //TAB_PANE
            case 14:
                role = "tabpanel";
                break;
            //TABLE
            case 15:
                role = "table";
                break;
            //CELL
            case 16:
                role = "cell";
                break;
            //TEXT_FIELD
            case 17:
                role = "textbox";
                var selection = document.createElement(elementType);
                selection.id = id + "-selection";
                selection.setAttribute("aria-live","polite");
                canvas.appendChild(selection);
                break;
            //LIST
            case 18:
                role = "list";
                break;
            //LIST_ITEM
            case 19:
                role = "listitem"
                break;
            //TREE_TABLE
            case 20:
                role = "treegrid";
                break;
            //DIALOG
            case 21:
                role = "dialog";
                break;
            //POPUP_MENU
            case 22:
                break;
            //PROGRESS_BAR
            case 23:
                role = "progressbar"
                break;
            //TREE_TABLE_CELL
            case 24:
                break;
            //GROUP
            case 25:
                role = "group"
                break;
            default:
                // do nothing?
        }
      
        
        //para.type = type;
        para.setAttribute("role",role);
        para.setAttribute("aria-label", itemName);
        para.setAttribute("aria-description", item.GetDescription())
        para.tabindex = -1;
       if (item.GetAccessibilityCode() == 4){
          para.onclick = this.InvokeButton$quorum_Libraries_Interface_Item;
       }
       else if (item.GetAccessibilityCode() == 2){
           para.onclick = this.UpdateToggleState$quorum_Libraries_Interface_Item$boolean;
       }
       else if (item.GetAccessibilityCode() == 3){
           para.setAttribute("name", item.GetName());  //item.GetButtonGroup() for value
           para.onclick = this.UpdateToggleState$quorum_Libraries_Interface_Item$boolean;
       }
       
       /*
       //Drawable using an img tag 
       else if (item.GetAccessibilityCode() == 1){
           para.setAttribute("src", description);      //Need Path for src attribute
           para.setAttribute("alt", item.GetDescription());
       }
       */
      
       //var node = document.createTextNode(description);
       //para.appendChild(node);

       canvas.appendChild(para);
        console.log(item.GetName(), " has been added.");
    };
//    system action NativeRemove(Item item)
    this.NativeRemove$quorum_Libraries_Interface_Item = function(item) {
        let id = item.GetHashCode();
        //cant remove what's not there
        if( elementList[id] == null ) {
            return;
        }
        //if it wasn't accessible it was never in the DOM
        if( elementList[id] != null ) {
            document.getElementById(item.GetHashCode()).remove();
            elementList[id] = null;
        }
        console.log(elementList[item.GetHashCode()], " has been removed.");
    };
    
//    system action MenuChanged(MenuChangeEvent event)
    this.MenuChanged$quorum_Libraries_Interface_Events_MenuChangeEvent = function(event) {
        console.log("Menu Changed");
    };
    
//    system action TreeChanged(TreeChangeEvent event)
    this.TreeChanged$quorum_Libraries_Interface_Events_TreeChangeEvent = function(event) {
        console.log("Tree Changed");
    };
    
//    system action TreeTableChanged(TreeTableChangeEvent event)
    this.TreeTableChanged$quorum_Libraries_Interface_Events_TreeTableChangeEvent = function(event) {
        console.log("TreeTable Changed");
    };
    
//    system action ControlActivated(ControlActivationEvent event)
    this.ControlActivated$quorum_Libraries_Interface_Events_ControlActivationEvent = function(event) {
        console.log("Control Activated");
    };
    
//    system action TextChanged(TextChangeEvent event)
    this.TextChanged$quorum_Libraries_Interface_Events_TextChangeEvent = function(event) {
        var control = event.GetControl();
        if ( global_InstanceOf(control,"Libraries.Interface.Controls.TextBox") )
        {
            var textbox = global_CheckCast(control, "Libraries.Interface.Controls.TextBox");
            var text = textbox.GetText();
            var id = textbox.GetHashCode();
            var element = document.getElementById(id);
            element.setAttribute("aria-description",text);
            console.log("TextBox text Changed");
        }
        else if ( global_InstanceOf(control,"Libraries.Interface.Controls.TextField") )
        {
            var textfield = global_CheckCast(control, "Libraries.Interface.Controls.TextField");
            var text = textfield.GetText();
            var id = textfield.GetHashCode();
            var element = document.getElementById(id);
            element.setAttribute("aria-description",text);
            console.log("TextField Text Changed");
        }
        else {
            console.log("Text Changed");
        }
        
    };
    
//    system action WindowFocusChanged(WindowFocusEvent event)
    this.WindowFocusChanged$quorum_Libraries_Interface_Events_WindowFocusEvent = function(event) {
        console.log("Window Focused");
    };
    
//    system action Notify(Item item, text value)
    this.Notify$quorum_Libraries_Interface_Item$text = function(item, value) {
        console.log("Notify call");
    };
    
//    system action Notify(Item item, text value, integer notificationType)
    this.Notify$quorum_Libraries_Interface_Events_Item$text$integer = function(item, value, notificationType) {
        console.log("Notify call 2");
    };
    
//    system action Shutdown
    this.Shutdown = function() {
        console.log("Shutdown");
        //dispose of the children
        var canvas = document.getElementById(currentIDECanvas_$Global_);
        while (canvas.firstChild) {
            canvas.firstChild.remove()
        }
        elementList.length = 0;
        currentFocus = null;
    };
    


/*
 * This implementation is old, but I am leaving it in as an exemplar. 
 * 
 * 
 *
    this.InvokeButton$quorum_Libraries_Interface_Item = function(item) {
        var buttonId = this.id;
        var button = elementList[buttonId];
        console.log(button.GetName(), "has been clicked.");
        
        var mouseEvent = new quorum_Libraries_Interface_Events_MouseEvent_();     //Creates MouseEvent variable
        mouseEvent.SetSource$quorum_Libraries_Interface_Item(button);                 //sets the Source for the mouseEvent to the quorum_FakeButton_ item
        mouseEvent.Set_Libraries_Interface_Events_MouseEvent__eventType_(1);      //sets the MouseEvent to MOUSE_CLICKED
        button.ActivateFakeButton$quorum_Libraries_Interface_Events_MouseEvent(mouseEvent);
    };
    

    this.UpdateToggleState$quorum_Libraries_Interface_Item$boolean = function(item, selected) {
        var toggleId = this.id;
        var toggle = elementList[toggleId];
        
        var mouseEvent = new quorum_Libraries_Interface_Events_MouseEvent_();
        mouseEvent.SetSource$quorum_Libraries_Interface_Item(toggle);
        mouseEvent.Set_Libraries_Interface_Events_MouseEvent__eventType_(1);
        if(toggle.GetAccessibilityCode() == 2){     //checkboxes
            if (this.checked == true){
                toggle.ActivateFakeCheckbox$quorum_Libraries_Interface_Events_MouseEvent(mouseEvent);
                console.log(toggle.GetName(), "has been checked.");
            }
            else if (this.checked == false){
                toggle.DeactivateFakeCheckbox$quorum_Libraries_Interface_Events_MouseEvent(mouseEvent);
                console.log(toggle.GetName(), "has been unchecked.");
            }
        }
        else if (toggle.GetAccessibilityCode() == 3){   //radio buttons work on traversing radio button options within the group
            console.log("radio buttons");
        }

    };
    */
}