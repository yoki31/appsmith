import { render, screen } from "test/testUtils";
import React from "react";
import { SettingTypes } from "../SettingsConfig";
import ButtonComponent from "./Button";
import { FormGroup } from "./Common";

let container: any = null;
const setting = {
  label: "formGroup",
  helpText: "",
  subText: "",
  category: "test",
  controlType: SettingTypes.BUTTON,
};
const CLASSNAME = "form-group";

function renderComponent() {
  render(
    <FormGroup className={CLASSNAME} setting={setting}>
      <div data-testid="admin-settings-form-group-child" />
    </FormGroup>,
    container,
  );
}

describe("FormGroup", () => {
  beforeEach(() => {
    container = document.createElement("div");
    document.body.appendChild(container);
  });

  it("is rendered", () => {
    renderComponent();
    const formGroup = screen.queryAllByTestId("admin-settings-form-group");
    expect(formGroup).toHaveLength(1);
  });
});
