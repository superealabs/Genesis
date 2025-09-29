import "./chunk-WDMUDEB6.js";

// node_modules/sweetalert2/dist/sweetalert2.esm.all.js
function _assertClassBrand(e, t, n) {
  if ("function" == typeof e ? e === t : e.has(t)) return arguments.length < 3 ? t : n;
  throw new TypeError("Private element is not present on this object");
}
function _checkPrivateRedeclaration(e, t) {
  if (t.has(e)) throw new TypeError("Cannot initialize the same private elements twice on an object");
}
function _classPrivateFieldGet2(s, a) {
  return s.get(_assertClassBrand(s, a));
}
function _classPrivateFieldInitSpec(e, t, a) {
  _checkPrivateRedeclaration(e, t), t.set(e, a);
}
function _classPrivateFieldSet2(s, a, r) {
  return s.set(_assertClassBrand(s, a), r), r;
}
var RESTORE_FOCUS_TIMEOUT = 100;
var globalState = {};
var focusPreviousActiveElement = () => {
  if (globalState.previousActiveElement instanceof HTMLElement) {
    globalState.previousActiveElement.focus();
    globalState.previousActiveElement = null;
  } else if (document.body) {
    document.body.focus();
  }
};
var restoreActiveElement = (returnFocus) => {
  return new Promise((resolve) => {
    if (!returnFocus) {
      return resolve();
    }
    const x = window.scrollX;
    const y = window.scrollY;
    globalState.restoreFocusTimeout = setTimeout(() => {
      focusPreviousActiveElement();
      resolve();
    }, RESTORE_FOCUS_TIMEOUT);
    window.scrollTo(x, y);
  });
};
var swalPrefix = "swal2-";
var classNames = ["container", "shown", "height-auto", "iosfix", "popup", "modal", "no-backdrop", "no-transition", "toast", "toast-shown", "show", "hide", "close", "title", "html-container", "actions", "confirm", "deny", "cancel", "footer", "icon", "icon-content", "image", "input", "file", "range", "select", "radio", "checkbox", "label", "textarea", "inputerror", "input-label", "validation-message", "progress-steps", "active-progress-step", "progress-step", "progress-step-line", "loader", "loading", "styled", "top", "top-start", "top-end", "top-left", "top-right", "center", "center-start", "center-end", "center-left", "center-right", "bottom", "bottom-start", "bottom-end", "bottom-left", "bottom-right", "grow-row", "grow-column", "grow-fullscreen", "rtl", "timer-progress-bar", "timer-progress-bar-container", "scrollbar-measure", "icon-success", "icon-warning", "icon-info", "icon-question", "icon-error", "draggable", "dragging"];
var swalClasses = classNames.reduce(
  (acc, className) => {
    acc[className] = swalPrefix + className;
    return acc;
  },
  /** @type {SwalClasses} */
  {}
);
var icons = ["success", "warning", "info", "question", "error"];
var iconTypes = icons.reduce(
  (acc, icon) => {
    acc[icon] = swalPrefix + icon;
    return acc;
  },
  /** @type {SwalIcons} */
  {}
);
var consolePrefix = "SweetAlert2:";
var capitalizeFirstLetter = (str) => str.charAt(0).toUpperCase() + str.slice(1);
var warn = (message) => {
  console.warn(`${consolePrefix} ${typeof message === "object" ? message.join(" ") : message}`);
};
var error = (message) => {
  console.error(`${consolePrefix} ${message}`);
};
var previousWarnOnceMessages = [];
var warnOnce = (message) => {
  if (!previousWarnOnceMessages.includes(message)) {
    previousWarnOnceMessages.push(message);
    warn(message);
  }
};
var warnAboutDeprecation = (deprecatedParam, useInstead = null) => {
  warnOnce(`"${deprecatedParam}" is deprecated and will be removed in the next major release.${useInstead ? ` Use "${useInstead}" instead.` : ""}`);
};
var callIfFunction = (arg) => typeof arg === "function" ? arg() : arg;
var hasToPromiseFn = (arg) => arg && typeof arg.toPromise === "function";
var asPromise = (arg) => hasToPromiseFn(arg) ? arg.toPromise() : Promise.resolve(arg);
var isPromise = (arg) => arg && Promise.resolve(arg) === arg;
var getContainer = () => document.body.querySelector(`.${swalClasses.container}`);
var elementBySelector = (selectorString) => {
  const container = getContainer();
  return container ? container.querySelector(selectorString) : null;
};
var elementByClass = (className) => {
  return elementBySelector(`.${className}`);
};
var getPopup = () => elementByClass(swalClasses.popup);
var getIcon = () => elementByClass(swalClasses.icon);
var getIconContent = () => elementByClass(swalClasses["icon-content"]);
var getTitle = () => elementByClass(swalClasses.title);
var getHtmlContainer = () => elementByClass(swalClasses["html-container"]);
var getImage = () => elementByClass(swalClasses.image);
var getProgressSteps = () => elementByClass(swalClasses["progress-steps"]);
var getValidationMessage = () => elementByClass(swalClasses["validation-message"]);
var getConfirmButton = () => (
  /** @type {HTMLButtonElement} */
  elementBySelector(`.${swalClasses.actions} .${swalClasses.confirm}`)
);
var getCancelButton = () => (
  /** @type {HTMLButtonElement} */
  elementBySelector(`.${swalClasses.actions} .${swalClasses.cancel}`)
);
var getDenyButton = () => (
  /** @type {HTMLButtonElement} */
  elementBySelector(`.${swalClasses.actions} .${swalClasses.deny}`)
);
var getInputLabel = () => elementByClass(swalClasses["input-label"]);
var getLoader = () => elementBySelector(`.${swalClasses.loader}`);
var getActions = () => elementByClass(swalClasses.actions);
var getFooter = () => elementByClass(swalClasses.footer);
var getTimerProgressBar = () => elementByClass(swalClasses["timer-progress-bar"]);
var getCloseButton = () => elementByClass(swalClasses.close);
var focusable = `
  a[href],
  area[href],
  input:not([disabled]),
  select:not([disabled]),
  textarea:not([disabled]),
  button:not([disabled]),
  iframe,
  object,
  embed,
  [tabindex="0"],
  [contenteditable],
  audio[controls],
  video[controls],
  summary
`;
var getFocusableElements = () => {
  const popup = getPopup();
  if (!popup) {
    return [];
  }
  const focusableElementsWithTabindex = popup.querySelectorAll('[tabindex]:not([tabindex="-1"]):not([tabindex="0"])');
  const focusableElementsWithTabindexSorted = Array.from(focusableElementsWithTabindex).sort((a, b) => {
    const tabindexA = parseInt(a.getAttribute("tabindex") || "0");
    const tabindexB = parseInt(b.getAttribute("tabindex") || "0");
    if (tabindexA > tabindexB) {
      return 1;
    } else if (tabindexA < tabindexB) {
      return -1;
    }
    return 0;
  });
  const otherFocusableElements = popup.querySelectorAll(focusable);
  const otherFocusableElementsFiltered = Array.from(otherFocusableElements).filter((el) => el.getAttribute("tabindex") !== "-1");
  return [...new Set(focusableElementsWithTabindexSorted.concat(otherFocusableElementsFiltered))].filter((el) => isVisible$1(el));
};
var isModal = () => {
  return hasClass(document.body, swalClasses.shown) && !hasClass(document.body, swalClasses["toast-shown"]) && !hasClass(document.body, swalClasses["no-backdrop"]);
};
var isToast = () => {
  const popup = getPopup();
  if (!popup) {
    return false;
  }
  return hasClass(popup, swalClasses.toast);
};
var isLoading = () => {
  const popup = getPopup();
  if (!popup) {
    return false;
  }
  return popup.hasAttribute("data-loading");
};
var setInnerHtml = (elem, html) => {
  elem.textContent = "";
  if (html) {
    const parser = new DOMParser();
    const parsed = parser.parseFromString(html, `text/html`);
    const head = parsed.querySelector("head");
    if (head) {
      Array.from(head.childNodes).forEach((child) => {
        elem.appendChild(child);
      });
    }
    const body = parsed.querySelector("body");
    if (body) {
      Array.from(body.childNodes).forEach((child) => {
        if (child instanceof HTMLVideoElement || child instanceof HTMLAudioElement) {
          elem.appendChild(child.cloneNode(true));
        } else {
          elem.appendChild(child);
        }
      });
    }
  }
};
var hasClass = (elem, className) => {
  if (!className) {
    return false;
  }
  const classList = className.split(/\s+/);
  for (let i = 0; i < classList.length; i++) {
    if (!elem.classList.contains(classList[i])) {
      return false;
    }
  }
  return true;
};
var removeCustomClasses = (elem, params) => {
  Array.from(elem.classList).forEach((className) => {
    if (!Object.values(swalClasses).includes(className) && !Object.values(iconTypes).includes(className) && !Object.values(params.showClass || {}).includes(className)) {
      elem.classList.remove(className);
    }
  });
};
var applyCustomClass = (elem, params, className) => {
  removeCustomClasses(elem, params);
  if (!params.customClass) {
    return;
  }
  const customClass = params.customClass[
    /** @type {keyof SweetAlertCustomClass} */
    className
  ];
  if (!customClass) {
    return;
  }
  if (typeof customClass !== "string" && !customClass.forEach) {
    warn(`Invalid type of customClass.${className}! Expected string or iterable object, got "${typeof customClass}"`);
    return;
  }
  addClass(elem, customClass);
};
var getInput$1 = (popup, inputClass) => {
  if (!inputClass) {
    return null;
  }
  switch (inputClass) {
    case "select":
    case "textarea":
    case "file":
      return popup.querySelector(`.${swalClasses.popup} > .${swalClasses[inputClass]}`);
    case "checkbox":
      return popup.querySelector(`.${swalClasses.popup} > .${swalClasses.checkbox} input`);
    case "radio":
      return popup.querySelector(`.${swalClasses.popup} > .${swalClasses.radio} input:checked`) || popup.querySelector(`.${swalClasses.popup} > .${swalClasses.radio} input:first-child`);
    case "range":
      return popup.querySelector(`.${swalClasses.popup} > .${swalClasses.range} input`);
    default:
      return popup.querySelector(`.${swalClasses.popup} > .${swalClasses.input}`);
  }
};
var focusInput = (input) => {
  input.focus();
  if (input.type !== "file") {
    const val = input.value;
    input.value = "";
    input.value = val;
  }
};
var toggleClass = (target, classList, condition) => {
  if (!target || !classList) {
    return;
  }
  if (typeof classList === "string") {
    classList = classList.split(/\s+/).filter(Boolean);
  }
  classList.forEach((className) => {
    if (Array.isArray(target)) {
      target.forEach((elem) => {
        if (condition) {
          elem.classList.add(className);
        } else {
          elem.classList.remove(className);
        }
      });
    } else {
      if (condition) {
        target.classList.add(className);
      } else {
        target.classList.remove(className);
      }
    }
  });
};
var addClass = (target, classList) => {
  toggleClass(target, classList, true);
};
var removeClass = (target, classList) => {
  toggleClass(target, classList, false);
};
var getDirectChildByClass = (elem, className) => {
  const children = Array.from(elem.children);
  for (let i = 0; i < children.length; i++) {
    const child = children[i];
    if (child instanceof HTMLElement && hasClass(child, className)) {
      return child;
    }
  }
};
var applyNumericalStyle = (elem, property, value) => {
  if (value === `${parseInt(value)}`) {
    value = parseInt(value);
  }
  if (value || parseInt(value) === 0) {
    elem.style.setProperty(property, typeof value === "number" ? `${value}px` : value);
  } else {
    elem.style.removeProperty(property);
  }
};
var show = (elem, display = "flex") => {
  if (!elem) {
    return;
  }
  elem.style.display = display;
};
var hide = (elem) => {
  if (!elem) {
    return;
  }
  elem.style.display = "none";
};
var showWhenInnerHtmlPresent = (elem, display = "block") => {
  if (!elem) {
    return;
  }
  new MutationObserver(() => {
    toggle(elem, elem.innerHTML, display);
  }).observe(elem, {
    childList: true,
    subtree: true
  });
};
var setStyle = (parent, selector, property, value) => {
  const el = parent.querySelector(selector);
  if (el) {
    el.style.setProperty(property, value);
  }
};
var toggle = (elem, condition, display = "flex") => {
  if (condition) {
    show(elem, display);
  } else {
    hide(elem);
  }
};
var isVisible$1 = (elem) => !!(elem && (elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length));
var allButtonsAreHidden = () => !isVisible$1(getConfirmButton()) && !isVisible$1(getDenyButton()) && !isVisible$1(getCancelButton());
var isScrollable = (elem) => !!(elem.scrollHeight > elem.clientHeight);
var selfOrParentIsScrollable = (element, stopElement) => {
  let parent = element;
  while (parent && parent !== stopElement) {
    if (isScrollable(parent)) {
      return true;
    }
    parent = parent.parentElement;
  }
  return false;
};
var hasCssAnimation = (elem) => {
  const style = window.getComputedStyle(elem);
  const animDuration = parseFloat(style.getPropertyValue("animation-duration") || "0");
  const transDuration = parseFloat(style.getPropertyValue("transition-duration") || "0");
  return animDuration > 0 || transDuration > 0;
};
var animateTimerProgressBar = (timer, reset = false) => {
  const timerProgressBar = getTimerProgressBar();
  if (!timerProgressBar) {
    return;
  }
  if (isVisible$1(timerProgressBar)) {
    if (reset) {
      timerProgressBar.style.transition = "none";
      timerProgressBar.style.width = "100%";
    }
    setTimeout(() => {
      timerProgressBar.style.transition = `width ${timer / 1e3}s linear`;
      timerProgressBar.style.width = "0%";
    }, 10);
  }
};
var stopTimerProgressBar = () => {
  const timerProgressBar = getTimerProgressBar();
  if (!timerProgressBar) {
    return;
  }
  const timerProgressBarWidth = parseInt(window.getComputedStyle(timerProgressBar).width);
  timerProgressBar.style.removeProperty("transition");
  timerProgressBar.style.width = "100%";
  const timerProgressBarFullWidth = parseInt(window.getComputedStyle(timerProgressBar).width);
  const timerProgressBarPercent = timerProgressBarWidth / timerProgressBarFullWidth * 100;
  timerProgressBar.style.width = `${timerProgressBarPercent}%`;
};
var isNodeEnv = () => typeof window === "undefined" || typeof document === "undefined";
var sweetHTML = `
 <div aria-labelledby="${swalClasses.title}" aria-describedby="${swalClasses["html-container"]}" class="${swalClasses.popup}" tabindex="-1">
   <button type="button" class="${swalClasses.close}"></button>
   <ul class="${swalClasses["progress-steps"]}"></ul>
   <div class="${swalClasses.icon}"></div>
   <img class="${swalClasses.image}" />
   <h2 class="${swalClasses.title}" id="${swalClasses.title}"></h2>
   <div class="${swalClasses["html-container"]}" id="${swalClasses["html-container"]}"></div>
   <input class="${swalClasses.input}" id="${swalClasses.input}" />
   <input type="file" class="${swalClasses.file}" />
   <div class="${swalClasses.range}">
     <input type="range" />
     <output></output>
   </div>
   <select class="${swalClasses.select}" id="${swalClasses.select}"></select>
   <div class="${swalClasses.radio}"></div>
   <label class="${swalClasses.checkbox}">
     <input type="checkbox" id="${swalClasses.checkbox}" />
     <span class="${swalClasses.label}"></span>
   </label>
   <textarea class="${swalClasses.textarea}" id="${swalClasses.textarea}"></textarea>
   <div class="${swalClasses["validation-message"]}" id="${swalClasses["validation-message"]}"></div>
   <div class="${swalClasses.actions}">
     <div class="${swalClasses.loader}"></div>
     <button type="button" class="${swalClasses.confirm}"></button>
     <button type="button" class="${swalClasses.deny}"></button>
     <button type="button" class="${swalClasses.cancel}"></button>
   </div>
   <div class="${swalClasses.footer}"></div>
   <div class="${swalClasses["timer-progress-bar-container"]}">
     <div class="${swalClasses["timer-progress-bar"]}"></div>
   </div>
 </div>
`.replace(/(^|\n)\s*/g, "");
var resetOldContainer = () => {
  const oldContainer = getContainer();
  if (!oldContainer) {
    return false;
  }
  oldContainer.remove();
  removeClass([document.documentElement, document.body], [swalClasses["no-backdrop"], swalClasses["toast-shown"], swalClasses["has-column"]]);
  return true;
};
var resetValidationMessage$1 = () => {
  globalState.currentInstance.resetValidationMessage();
};
var addInputChangeListeners = () => {
  const popup = getPopup();
  const input = getDirectChildByClass(popup, swalClasses.input);
  const file = getDirectChildByClass(popup, swalClasses.file);
  const range = popup.querySelector(`.${swalClasses.range} input`);
  const rangeOutput = popup.querySelector(`.${swalClasses.range} output`);
  const select = getDirectChildByClass(popup, swalClasses.select);
  const checkbox = popup.querySelector(`.${swalClasses.checkbox} input`);
  const textarea = getDirectChildByClass(popup, swalClasses.textarea);
  input.oninput = resetValidationMessage$1;
  file.onchange = resetValidationMessage$1;
  select.onchange = resetValidationMessage$1;
  checkbox.onchange = resetValidationMessage$1;
  textarea.oninput = resetValidationMessage$1;
  range.oninput = () => {
    resetValidationMessage$1();
    rangeOutput.value = range.value;
  };
  range.onchange = () => {
    resetValidationMessage$1();
    rangeOutput.value = range.value;
  };
};
var getTarget = (target) => typeof target === "string" ? document.querySelector(target) : target;
var setupAccessibility = (params) => {
  const popup = getPopup();
  popup.setAttribute("role", params.toast ? "alert" : "dialog");
  popup.setAttribute("aria-live", params.toast ? "polite" : "assertive");
  if (!params.toast) {
    popup.setAttribute("aria-modal", "true");
  }
};
var setupRTL = (targetElement) => {
  if (window.getComputedStyle(targetElement).direction === "rtl") {
    addClass(getContainer(), swalClasses.rtl);
  }
};
var init = (params) => {
  const oldContainerExisted = resetOldContainer();
  if (isNodeEnv()) {
    error("SweetAlert2 requires document to initialize");
    return;
  }
  const container = document.createElement("div");
  container.className = swalClasses.container;
  if (oldContainerExisted) {
    addClass(container, swalClasses["no-transition"]);
  }
  setInnerHtml(container, sweetHTML);
  container.dataset["swal2Theme"] = params.theme;
  const targetElement = getTarget(params.target);
  targetElement.appendChild(container);
  if (params.topLayer) {
    container.setAttribute("popover", "");
    container.showPopover();
  }
  setupAccessibility(params);
  setupRTL(targetElement);
  addInputChangeListeners();
};
var parseHtmlToContainer = (param, target) => {
  if (param instanceof HTMLElement) {
    target.appendChild(param);
  } else if (typeof param === "object") {
    handleObject(param, target);
  } else if (param) {
    setInnerHtml(target, param);
  }
};
var handleObject = (param, target) => {
  if (param.jquery) {
    handleJqueryElem(target, param);
  } else {
    setInnerHtml(target, param.toString());
  }
};
var handleJqueryElem = (target, elem) => {
  target.textContent = "";
  if (0 in elem) {
    for (let i = 0; i in elem; i++) {
      target.appendChild(elem[i].cloneNode(true));
    }
  } else {
    target.appendChild(elem.cloneNode(true));
  }
};
var renderActions = (instance, params) => {
  const actions = getActions();
  const loader = getLoader();
  if (!actions || !loader) {
    return;
  }
  if (!params.showConfirmButton && !params.showDenyButton && !params.showCancelButton) {
    hide(actions);
  } else {
    show(actions);
  }
  applyCustomClass(actions, params, "actions");
  renderButtons(actions, loader, params);
  setInnerHtml(loader, params.loaderHtml || "");
  applyCustomClass(loader, params, "loader");
};
function renderButtons(actions, loader, params) {
  const confirmButton = getConfirmButton();
  const denyButton = getDenyButton();
  const cancelButton = getCancelButton();
  if (!confirmButton || !denyButton || !cancelButton) {
    return;
  }
  renderButton(confirmButton, "confirm", params);
  renderButton(denyButton, "deny", params);
  renderButton(cancelButton, "cancel", params);
  handleButtonsStyling(confirmButton, denyButton, cancelButton, params);
  if (params.reverseButtons) {
    if (params.toast) {
      actions.insertBefore(cancelButton, confirmButton);
      actions.insertBefore(denyButton, confirmButton);
    } else {
      actions.insertBefore(cancelButton, loader);
      actions.insertBefore(denyButton, loader);
      actions.insertBefore(confirmButton, loader);
    }
  }
}
function handleButtonsStyling(confirmButton, denyButton, cancelButton, params) {
  if (!params.buttonsStyling) {
    removeClass([confirmButton, denyButton, cancelButton], swalClasses.styled);
    return;
  }
  addClass([confirmButton, denyButton, cancelButton], swalClasses.styled);
  if (params.confirmButtonColor) {
    confirmButton.style.setProperty("--swal2-confirm-button-background-color", params.confirmButtonColor);
  }
  if (params.denyButtonColor) {
    denyButton.style.setProperty("--swal2-deny-button-background-color", params.denyButtonColor);
  }
  if (params.cancelButtonColor) {
    cancelButton.style.setProperty("--swal2-cancel-button-background-color", params.cancelButtonColor);
  }
  applyOutlineColor(confirmButton);
  applyOutlineColor(denyButton);
  applyOutlineColor(cancelButton);
}
function applyOutlineColor(button) {
  const buttonStyle = window.getComputedStyle(button);
  if (buttonStyle.getPropertyValue("--swal2-action-button-focus-box-shadow")) {
    return;
  }
  const outlineColor = buttonStyle.backgroundColor.replace(/rgba?\((\d+), (\d+), (\d+).*/, "rgba($1, $2, $3, 0.5)");
  button.style.setProperty("--swal2-action-button-focus-box-shadow", buttonStyle.getPropertyValue("--swal2-outline").replace(/ rgba\(.*/, ` ${outlineColor}`));
}
function renderButton(button, buttonType, params) {
  const buttonName = (
    /** @type {'Confirm' | 'Deny' | 'Cancel'} */
    capitalizeFirstLetter(buttonType)
  );
  toggle(button, params[`show${buttonName}Button`], "inline-block");
  setInnerHtml(button, params[`${buttonType}ButtonText`] || "");
  button.setAttribute("aria-label", params[`${buttonType}ButtonAriaLabel`] || "");
  button.className = swalClasses[buttonType];
  applyCustomClass(button, params, `${buttonType}Button`);
}
var renderCloseButton = (instance, params) => {
  const closeButton = getCloseButton();
  if (!closeButton) {
    return;
  }
  setInnerHtml(closeButton, params.closeButtonHtml || "");
  applyCustomClass(closeButton, params, "closeButton");
  toggle(closeButton, params.showCloseButton);
  closeButton.setAttribute("aria-label", params.closeButtonAriaLabel || "");
};
var renderContainer = (instance, params) => {
  const container = getContainer();
  if (!container) {
    return;
  }
  handleBackdropParam(container, params.backdrop);
  handlePositionParam(container, params.position);
  handleGrowParam(container, params.grow);
  applyCustomClass(container, params, "container");
};
function handleBackdropParam(container, backdrop) {
  if (typeof backdrop === "string") {
    container.style.background = backdrop;
  } else if (!backdrop) {
    addClass([document.documentElement, document.body], swalClasses["no-backdrop"]);
  }
}
function handlePositionParam(container, position) {
  if (!position) {
    return;
  }
  if (position in swalClasses) {
    addClass(container, swalClasses[position]);
  } else {
    warn('The "position" parameter is not valid, defaulting to "center"');
    addClass(container, swalClasses.center);
  }
}
function handleGrowParam(container, grow) {
  if (!grow) {
    return;
  }
  addClass(container, swalClasses[`grow-${grow}`]);
}
var privateProps = {
  innerParams: /* @__PURE__ */ new WeakMap(),
  domCache: /* @__PURE__ */ new WeakMap()
};
var inputClasses = ["input", "file", "range", "select", "radio", "checkbox", "textarea"];
var renderInput = (instance, params) => {
  const popup = getPopup();
  if (!popup) {
    return;
  }
  const innerParams = privateProps.innerParams.get(instance);
  const rerender = !innerParams || params.input !== innerParams.input;
  inputClasses.forEach((inputClass) => {
    const inputContainer = getDirectChildByClass(popup, swalClasses[inputClass]);
    if (!inputContainer) {
      return;
    }
    setAttributes(inputClass, params.inputAttributes);
    inputContainer.className = swalClasses[inputClass];
    if (rerender) {
      hide(inputContainer);
    }
  });
  if (params.input) {
    if (rerender) {
      showInput(params);
    }
    setCustomClass(params);
  }
};
var showInput = (params) => {
  if (!params.input) {
    return;
  }
  if (!renderInputType[params.input]) {
    error(`Unexpected type of input! Expected ${Object.keys(renderInputType).join(" | ")}, got "${params.input}"`);
    return;
  }
  const inputContainer = getInputContainer(params.input);
  if (!inputContainer) {
    return;
  }
  const input = renderInputType[params.input](inputContainer, params);
  show(inputContainer);
  if (params.inputAutoFocus) {
    setTimeout(() => {
      focusInput(input);
    });
  }
};
var removeAttributes = (input) => {
  for (let i = 0; i < input.attributes.length; i++) {
    const attrName = input.attributes[i].name;
    if (!["id", "type", "value", "style"].includes(attrName)) {
      input.removeAttribute(attrName);
    }
  }
};
var setAttributes = (inputClass, inputAttributes) => {
  const popup = getPopup();
  if (!popup) {
    return;
  }
  const input = getInput$1(popup, inputClass);
  if (!input) {
    return;
  }
  removeAttributes(input);
  for (const attr in inputAttributes) {
    input.setAttribute(attr, inputAttributes[attr]);
  }
};
var setCustomClass = (params) => {
  if (!params.input) {
    return;
  }
  const inputContainer = getInputContainer(params.input);
  if (inputContainer) {
    applyCustomClass(inputContainer, params, "input");
  }
};
var setInputPlaceholder = (input, params) => {
  if (!input.placeholder && params.inputPlaceholder) {
    input.placeholder = params.inputPlaceholder;
  }
};
var setInputLabel = (input, prependTo, params) => {
  if (params.inputLabel) {
    const label = document.createElement("label");
    const labelClass = swalClasses["input-label"];
    label.setAttribute("for", input.id);
    label.className = labelClass;
    if (typeof params.customClass === "object") {
      addClass(label, params.customClass.inputLabel);
    }
    label.innerText = params.inputLabel;
    prependTo.insertAdjacentElement("beforebegin", label);
  }
};
var getInputContainer = (inputType) => {
  const popup = getPopup();
  if (!popup) {
    return;
  }
  return getDirectChildByClass(popup, swalClasses[
    /** @type {SwalClass} */
    inputType
  ] || swalClasses.input);
};
var checkAndSetInputValue = (input, inputValue) => {
  if (["string", "number"].includes(typeof inputValue)) {
    input.value = `${inputValue}`;
  } else if (!isPromise(inputValue)) {
    warn(`Unexpected type of inputValue! Expected "string", "number" or "Promise", got "${typeof inputValue}"`);
  }
};
var renderInputType = {};
renderInputType.text = renderInputType.email = renderInputType.password = renderInputType.number = renderInputType.tel = renderInputType.url = renderInputType.search = renderInputType.date = renderInputType["datetime-local"] = renderInputType.time = renderInputType.week = renderInputType.month = /** @type {(input: Input | HTMLElement, params: SweetAlertOptions) => Input} */
(input, params) => {
  checkAndSetInputValue(input, params.inputValue);
  setInputLabel(input, input, params);
  setInputPlaceholder(input, params);
  input.type = params.input;
  return input;
};
renderInputType.file = (input, params) => {
  setInputLabel(input, input, params);
  setInputPlaceholder(input, params);
  return input;
};
renderInputType.range = (range, params) => {
  const rangeInput = range.querySelector("input");
  const rangeOutput = range.querySelector("output");
  checkAndSetInputValue(rangeInput, params.inputValue);
  rangeInput.type = params.input;
  checkAndSetInputValue(rangeOutput, params.inputValue);
  setInputLabel(rangeInput, range, params);
  return range;
};
renderInputType.select = (select, params) => {
  select.textContent = "";
  if (params.inputPlaceholder) {
    const placeholder = document.createElement("option");
    setInnerHtml(placeholder, params.inputPlaceholder);
    placeholder.value = "";
    placeholder.disabled = true;
    placeholder.selected = true;
    select.appendChild(placeholder);
  }
  setInputLabel(select, select, params);
  return select;
};
renderInputType.radio = (radio) => {
  radio.textContent = "";
  return radio;
};
renderInputType.checkbox = (checkboxContainer, params) => {
  const checkbox = getInput$1(getPopup(), "checkbox");
  checkbox.value = "1";
  checkbox.checked = Boolean(params.inputValue);
  const label = checkboxContainer.querySelector("span");
  setInnerHtml(label, params.inputPlaceholder || params.inputLabel);
  return checkbox;
};
renderInputType.textarea = (textarea, params) => {
  checkAndSetInputValue(textarea, params.inputValue);
  setInputPlaceholder(textarea, params);
  setInputLabel(textarea, textarea, params);
  const getMargin = (el) => parseInt(window.getComputedStyle(el).marginLeft) + parseInt(window.getComputedStyle(el).marginRight);
  setTimeout(() => {
    if ("MutationObserver" in window) {
      const initialPopupWidth = parseInt(window.getComputedStyle(getPopup()).width);
      const textareaResizeHandler = () => {
        if (!document.body.contains(textarea)) {
          return;
        }
        const textareaWidth = textarea.offsetWidth + getMargin(textarea);
        if (textareaWidth > initialPopupWidth) {
          getPopup().style.width = `${textareaWidth}px`;
        } else {
          applyNumericalStyle(getPopup(), "width", params.width);
        }
      };
      new MutationObserver(textareaResizeHandler).observe(textarea, {
        attributes: true,
        attributeFilter: ["style"]
      });
    }
  });
  return textarea;
};
var renderContent = (instance, params) => {
  const htmlContainer = getHtmlContainer();
  if (!htmlContainer) {
    return;
  }
  showWhenInnerHtmlPresent(htmlContainer);
  applyCustomClass(htmlContainer, params, "htmlContainer");
  if (params.html) {
    parseHtmlToContainer(params.html, htmlContainer);
    show(htmlContainer, "block");
  } else if (params.text) {
    htmlContainer.textContent = params.text;
    show(htmlContainer, "block");
  } else {
    hide(htmlContainer);
  }
  renderInput(instance, params);
};
var renderFooter = (instance, params) => {
  const footer = getFooter();
  if (!footer) {
    return;
  }
  showWhenInnerHtmlPresent(footer);
  toggle(footer, params.footer, "block");
  if (params.footer) {
    parseHtmlToContainer(params.footer, footer);
  }
  applyCustomClass(footer, params, "footer");
};
var renderIcon = (instance, params) => {
  const innerParams = privateProps.innerParams.get(instance);
  const icon = getIcon();
  if (!icon) {
    return;
  }
  if (innerParams && params.icon === innerParams.icon) {
    setContent(icon, params);
    applyStyles(icon, params);
    return;
  }
  if (!params.icon && !params.iconHtml) {
    hide(icon);
    return;
  }
  if (params.icon && Object.keys(iconTypes).indexOf(params.icon) === -1) {
    error(`Unknown icon! Expected "success", "error", "warning", "info" or "question", got "${params.icon}"`);
    hide(icon);
    return;
  }
  show(icon);
  setContent(icon, params);
  applyStyles(icon, params);
  addClass(icon, params.showClass && params.showClass.icon);
  const colorSchemeQueryList = window.matchMedia("(prefers-color-scheme: dark)");
  colorSchemeQueryList.addEventListener("change", adjustSuccessIconBackgroundColor);
};
var applyStyles = (icon, params) => {
  for (const [iconType, iconClassName] of Object.entries(iconTypes)) {
    if (params.icon !== iconType) {
      removeClass(icon, iconClassName);
    }
  }
  addClass(icon, params.icon && iconTypes[params.icon]);
  setColor(icon, params);
  adjustSuccessIconBackgroundColor();
  applyCustomClass(icon, params, "icon");
};
var adjustSuccessIconBackgroundColor = () => {
  const popup = getPopup();
  if (!popup) {
    return;
  }
  const popupBackgroundColor = window.getComputedStyle(popup).getPropertyValue("background-color");
  const successIconParts = popup.querySelectorAll("[class^=swal2-success-circular-line], .swal2-success-fix");
  for (let i = 0; i < successIconParts.length; i++) {
    successIconParts[i].style.backgroundColor = popupBackgroundColor;
  }
};
var successIconHtml = (params) => `
  ${params.animation ? '<div class="swal2-success-circular-line-left"></div>' : ""}
  <span class="swal2-success-line-tip"></span> <span class="swal2-success-line-long"></span>
  <div class="swal2-success-ring"></div>
  ${params.animation ? '<div class="swal2-success-fix"></div>' : ""}
  ${params.animation ? '<div class="swal2-success-circular-line-right"></div>' : ""}
`;
var errorIconHtml = `
  <span class="swal2-x-mark">
    <span class="swal2-x-mark-line-left"></span>
    <span class="swal2-x-mark-line-right"></span>
  </span>
`;
var setContent = (icon, params) => {
  if (!params.icon && !params.iconHtml) {
    return;
  }
  let oldContent = icon.innerHTML;
  let newContent = "";
  if (params.iconHtml) {
    newContent = iconContent(params.iconHtml);
  } else if (params.icon === "success") {
    newContent = successIconHtml(params);
    oldContent = oldContent.replace(/ style=".*?"/g, "");
  } else if (params.icon === "error") {
    newContent = errorIconHtml;
  } else if (params.icon) {
    const defaultIconHtml = {
      question: "?",
      warning: "!",
      info: "i"
    };
    newContent = iconContent(defaultIconHtml[params.icon]);
  }
  if (oldContent.trim() !== newContent.trim()) {
    setInnerHtml(icon, newContent);
  }
};
var setColor = (icon, params) => {
  if (!params.iconColor) {
    return;
  }
  icon.style.color = params.iconColor;
  icon.style.borderColor = params.iconColor;
  for (const sel of [".swal2-success-line-tip", ".swal2-success-line-long", ".swal2-x-mark-line-left", ".swal2-x-mark-line-right"]) {
    setStyle(icon, sel, "background-color", params.iconColor);
  }
  setStyle(icon, ".swal2-success-ring", "border-color", params.iconColor);
};
var iconContent = (content) => `<div class="${swalClasses["icon-content"]}">${content}</div>`;
var renderImage = (instance, params) => {
  const image = getImage();
  if (!image) {
    return;
  }
  if (!params.imageUrl) {
    hide(image);
    return;
  }
  show(image, "");
  image.setAttribute("src", params.imageUrl);
  image.setAttribute("alt", params.imageAlt || "");
  applyNumericalStyle(image, "width", params.imageWidth);
  applyNumericalStyle(image, "height", params.imageHeight);
  image.className = swalClasses.image;
  applyCustomClass(image, params, "image");
};
var dragging = false;
var mousedownX = 0;
var mousedownY = 0;
var initialX = 0;
var initialY = 0;
var addDraggableListeners = (popup) => {
  popup.addEventListener("mousedown", down);
  document.body.addEventListener("mousemove", move);
  popup.addEventListener("mouseup", up);
  popup.addEventListener("touchstart", down);
  document.body.addEventListener("touchmove", move);
  popup.addEventListener("touchend", up);
};
var removeDraggableListeners = (popup) => {
  popup.removeEventListener("mousedown", down);
  document.body.removeEventListener("mousemove", move);
  popup.removeEventListener("mouseup", up);
  popup.removeEventListener("touchstart", down);
  document.body.removeEventListener("touchmove", move);
  popup.removeEventListener("touchend", up);
};
var down = (event) => {
  const popup = getPopup();
  if (event.target === popup || getIcon().contains(
    /** @type {HTMLElement} */
    event.target
  )) {
    dragging = true;
    const clientXY = getClientXY(event);
    mousedownX = clientXY.clientX;
    mousedownY = clientXY.clientY;
    initialX = parseInt(popup.style.insetInlineStart) || 0;
    initialY = parseInt(popup.style.insetBlockStart) || 0;
    addClass(popup, "swal2-dragging");
  }
};
var move = (event) => {
  const popup = getPopup();
  if (dragging) {
    let {
      clientX,
      clientY
    } = getClientXY(event);
    popup.style.insetInlineStart = `${initialX + (clientX - mousedownX)}px`;
    popup.style.insetBlockStart = `${initialY + (clientY - mousedownY)}px`;
  }
};
var up = () => {
  const popup = getPopup();
  dragging = false;
  removeClass(popup, "swal2-dragging");
};
var getClientXY = (event) => {
  let clientX = 0, clientY = 0;
  if (event.type.startsWith("mouse")) {
    clientX = /** @type {MouseEvent} */
    event.clientX;
    clientY = /** @type {MouseEvent} */
    event.clientY;
  } else if (event.type.startsWith("touch")) {
    clientX = /** @type {TouchEvent} */
    event.touches[0].clientX;
    clientY = /** @type {TouchEvent} */
    event.touches[0].clientY;
  }
  return {
    clientX,
    clientY
  };
};
var renderPopup = (instance, params) => {
  const container = getContainer();
  const popup = getPopup();
  if (!container || !popup) {
    return;
  }
  if (params.toast) {
    applyNumericalStyle(container, "width", params.width);
    popup.style.width = "100%";
    const loader = getLoader();
    if (loader) {
      popup.insertBefore(loader, getIcon());
    }
  } else {
    applyNumericalStyle(popup, "width", params.width);
  }
  applyNumericalStyle(popup, "padding", params.padding);
  if (params.color) {
    popup.style.color = params.color;
  }
  if (params.background) {
    popup.style.background = params.background;
  }
  hide(getValidationMessage());
  addClasses$1(popup, params);
  if (params.draggable && !params.toast) {
    addClass(popup, swalClasses.draggable);
    addDraggableListeners(popup);
  } else {
    removeClass(popup, swalClasses.draggable);
    removeDraggableListeners(popup);
  }
};
var addClasses$1 = (popup, params) => {
  const showClass = params.showClass || {};
  popup.className = `${swalClasses.popup} ${isVisible$1(popup) ? showClass.popup : ""}`;
  if (params.toast) {
    addClass([document.documentElement, document.body], swalClasses["toast-shown"]);
    addClass(popup, swalClasses.toast);
  } else {
    addClass(popup, swalClasses.modal);
  }
  applyCustomClass(popup, params, "popup");
  if (typeof params.customClass === "string") {
    addClass(popup, params.customClass);
  }
  if (params.icon) {
    addClass(popup, swalClasses[`icon-${params.icon}`]);
  }
};
var renderProgressSteps = (instance, params) => {
  const progressStepsContainer = getProgressSteps();
  if (!progressStepsContainer) {
    return;
  }
  const {
    progressSteps,
    currentProgressStep
  } = params;
  if (!progressSteps || progressSteps.length === 0 || currentProgressStep === void 0) {
    hide(progressStepsContainer);
    return;
  }
  show(progressStepsContainer);
  progressStepsContainer.textContent = "";
  if (currentProgressStep >= progressSteps.length) {
    warn("Invalid currentProgressStep parameter, it should be less than progressSteps.length (currentProgressStep like JS arrays starts from 0)");
  }
  progressSteps.forEach((step, index) => {
    const stepEl = createStepElement(step);
    progressStepsContainer.appendChild(stepEl);
    if (index === currentProgressStep) {
      addClass(stepEl, swalClasses["active-progress-step"]);
    }
    if (index !== progressSteps.length - 1) {
      const lineEl = createLineElement(params);
      progressStepsContainer.appendChild(lineEl);
    }
  });
};
var createStepElement = (step) => {
  const stepEl = document.createElement("li");
  addClass(stepEl, swalClasses["progress-step"]);
  setInnerHtml(stepEl, step);
  return stepEl;
};
var createLineElement = (params) => {
  const lineEl = document.createElement("li");
  addClass(lineEl, swalClasses["progress-step-line"]);
  if (params.progressStepsDistance) {
    applyNumericalStyle(lineEl, "width", params.progressStepsDistance);
  }
  return lineEl;
};
var renderTitle = (instance, params) => {
  const title = getTitle();
  if (!title) {
    return;
  }
  showWhenInnerHtmlPresent(title);
  toggle(title, params.title || params.titleText, "block");
  if (params.title) {
    parseHtmlToContainer(params.title, title);
  }
  if (params.titleText) {
    title.innerText = params.titleText;
  }
  applyCustomClass(title, params, "title");
};
var render = (instance, params) => {
  renderPopup(instance, params);
  renderContainer(instance, params);
  renderProgressSteps(instance, params);
  renderIcon(instance, params);
  renderImage(instance, params);
  renderTitle(instance, params);
  renderCloseButton(instance, params);
  renderContent(instance, params);
  renderActions(instance, params);
  renderFooter(instance, params);
  const popup = getPopup();
  if (typeof params.didRender === "function" && popup) {
    params.didRender(popup);
  }
  globalState.eventEmitter.emit("didRender", popup);
};
var isVisible = () => {
  return isVisible$1(getPopup());
};
var clickConfirm = () => {
  var _dom$getConfirmButton;
  return (_dom$getConfirmButton = getConfirmButton()) === null || _dom$getConfirmButton === void 0 ? void 0 : _dom$getConfirmButton.click();
};
var clickDeny = () => {
  var _dom$getDenyButton;
  return (_dom$getDenyButton = getDenyButton()) === null || _dom$getDenyButton === void 0 ? void 0 : _dom$getDenyButton.click();
};
var clickCancel = () => {
  var _dom$getCancelButton;
  return (_dom$getCancelButton = getCancelButton()) === null || _dom$getCancelButton === void 0 ? void 0 : _dom$getCancelButton.click();
};
var DismissReason = Object.freeze({
  cancel: "cancel",
  backdrop: "backdrop",
  close: "close",
  esc: "esc",
  timer: "timer"
});
var removeKeydownHandler = (globalState2) => {
  if (globalState2.keydownTarget && globalState2.keydownHandlerAdded) {
    globalState2.keydownTarget.removeEventListener("keydown", globalState2.keydownHandler, {
      capture: globalState2.keydownListenerCapture
    });
    globalState2.keydownHandlerAdded = false;
  }
};
var addKeydownHandler = (globalState2, innerParams, dismissWith) => {
  removeKeydownHandler(globalState2);
  if (!innerParams.toast) {
    globalState2.keydownHandler = (e) => keydownHandler(innerParams, e, dismissWith);
    globalState2.keydownTarget = innerParams.keydownListenerCapture ? window : getPopup();
    globalState2.keydownListenerCapture = innerParams.keydownListenerCapture;
    globalState2.keydownTarget.addEventListener("keydown", globalState2.keydownHandler, {
      capture: globalState2.keydownListenerCapture
    });
    globalState2.keydownHandlerAdded = true;
  }
};
var setFocus = (index, increment) => {
  var _dom$getPopup;
  const focusableElements = getFocusableElements();
  if (focusableElements.length) {
    index = index + increment;
    if (index === -2) {
      index = focusableElements.length - 1;
    }
    if (index === focusableElements.length) {
      index = 0;
    } else if (index === -1) {
      index = focusableElements.length - 1;
    }
    focusableElements[index].focus();
    return;
  }
  (_dom$getPopup = getPopup()) === null || _dom$getPopup === void 0 || _dom$getPopup.focus();
};
var arrowKeysNextButton = ["ArrowRight", "ArrowDown"];
var arrowKeysPreviousButton = ["ArrowLeft", "ArrowUp"];
var keydownHandler = (innerParams, event, dismissWith) => {
  if (!innerParams) {
    return;
  }
  if (event.isComposing || event.keyCode === 229) {
    return;
  }
  if (innerParams.stopKeydownPropagation) {
    event.stopPropagation();
  }
  if (event.key === "Enter") {
    handleEnter(event, innerParams);
  } else if (event.key === "Tab") {
    handleTab(event);
  } else if ([...arrowKeysNextButton, ...arrowKeysPreviousButton].includes(event.key)) {
    handleArrows(event.key);
  } else if (event.key === "Escape") {
    handleEsc(event, innerParams, dismissWith);
  }
};
var handleEnter = (event, innerParams) => {
  if (!callIfFunction(innerParams.allowEnterKey)) {
    return;
  }
  const input = getInput$1(getPopup(), innerParams.input);
  if (event.target && input && event.target instanceof HTMLElement && event.target.outerHTML === input.outerHTML) {
    if (["textarea", "file"].includes(innerParams.input)) {
      return;
    }
    clickConfirm();
    event.preventDefault();
  }
};
var handleTab = (event) => {
  const targetElement = event.target;
  const focusableElements = getFocusableElements();
  let btnIndex = -1;
  for (let i = 0; i < focusableElements.length; i++) {
    if (targetElement === focusableElements[i]) {
      btnIndex = i;
      break;
    }
  }
  if (!event.shiftKey) {
    setFocus(btnIndex, 1);
  } else {
    setFocus(btnIndex, -1);
  }
  event.stopPropagation();
  event.preventDefault();
};
var handleArrows = (key) => {
  const actions = getActions();
  const confirmButton = getConfirmButton();
  const denyButton = getDenyButton();
  const cancelButton = getCancelButton();
  if (!actions || !confirmButton || !denyButton || !cancelButton) {
    return;
  }
  const buttons = [confirmButton, denyButton, cancelButton];
  if (document.activeElement instanceof HTMLElement && !buttons.includes(document.activeElement)) {
    return;
  }
  const sibling = arrowKeysNextButton.includes(key) ? "nextElementSibling" : "previousElementSibling";
  let buttonToFocus = document.activeElement;
  if (!buttonToFocus) {
    return;
  }
  for (let i = 0; i < actions.children.length; i++) {
    buttonToFocus = buttonToFocus[sibling];
    if (!buttonToFocus) {
      return;
    }
    if (buttonToFocus instanceof HTMLButtonElement && isVisible$1(buttonToFocus)) {
      break;
    }
  }
  if (buttonToFocus instanceof HTMLButtonElement) {
    buttonToFocus.focus();
  }
};
var handleEsc = (event, innerParams, dismissWith) => {
  event.preventDefault();
  if (callIfFunction(innerParams.allowEscapeKey)) {
    dismissWith(DismissReason.esc);
  }
};
var privateMethods = {
  swalPromiseResolve: /* @__PURE__ */ new WeakMap(),
  swalPromiseReject: /* @__PURE__ */ new WeakMap()
};
var setAriaHidden = () => {
  const container = getContainer();
  const bodyChildren = Array.from(document.body.children);
  bodyChildren.forEach((el) => {
    if (el.contains(container)) {
      return;
    }
    if (el.hasAttribute("aria-hidden")) {
      el.setAttribute("data-previous-aria-hidden", el.getAttribute("aria-hidden") || "");
    }
    el.setAttribute("aria-hidden", "true");
  });
};
var unsetAriaHidden = () => {
  const bodyChildren = Array.from(document.body.children);
  bodyChildren.forEach((el) => {
    if (el.hasAttribute("data-previous-aria-hidden")) {
      el.setAttribute("aria-hidden", el.getAttribute("data-previous-aria-hidden") || "");
      el.removeAttribute("data-previous-aria-hidden");
    } else {
      el.removeAttribute("aria-hidden");
    }
  });
};
var isSafariOrIOS = typeof window !== "undefined" && !!window.GestureEvent;
var iOSfix = () => {
  if (isSafariOrIOS && !hasClass(document.body, swalClasses.iosfix)) {
    const offset = document.body.scrollTop;
    document.body.style.top = `${offset * -1}px`;
    addClass(document.body, swalClasses.iosfix);
    lockBodyScroll();
  }
};
var lockBodyScroll = () => {
  const container = getContainer();
  if (!container) {
    return;
  }
  let preventTouchMove;
  container.ontouchstart = (event) => {
    preventTouchMove = shouldPreventTouchMove(event);
  };
  container.ontouchmove = (event) => {
    if (preventTouchMove) {
      event.preventDefault();
      event.stopPropagation();
    }
  };
};
var shouldPreventTouchMove = (event) => {
  const target = event.target;
  const container = getContainer();
  const htmlContainer = getHtmlContainer();
  if (!container || !htmlContainer) {
    return false;
  }
  if (isStylus(event) || isZoom(event)) {
    return false;
  }
  if (target === container) {
    return true;
  }
  if (!isScrollable(container) && target instanceof HTMLElement && !selfOrParentIsScrollable(target, htmlContainer) && // #2823
  target.tagName !== "INPUT" && // #1603
  target.tagName !== "TEXTAREA" && // #2266
  !(isScrollable(htmlContainer) && // #1944
  htmlContainer.contains(target))) {
    return true;
  }
  return false;
};
var isStylus = (event) => {
  return event.touches && event.touches.length && event.touches[0].touchType === "stylus";
};
var isZoom = (event) => {
  return event.touches && event.touches.length > 1;
};
var undoIOSfix = () => {
  if (hasClass(document.body, swalClasses.iosfix)) {
    const offset = parseInt(document.body.style.top, 10);
    removeClass(document.body, swalClasses.iosfix);
    document.body.style.top = "";
    document.body.scrollTop = offset * -1;
  }
};
var measureScrollbar = () => {
  const scrollDiv = document.createElement("div");
  scrollDiv.className = swalClasses["scrollbar-measure"];
  document.body.appendChild(scrollDiv);
  const scrollbarWidth = scrollDiv.getBoundingClientRect().width - scrollDiv.clientWidth;
  document.body.removeChild(scrollDiv);
  return scrollbarWidth;
};
var previousBodyPadding = null;
var replaceScrollbarWithPadding = (initialBodyOverflow) => {
  if (previousBodyPadding !== null) {
    return;
  }
  if (document.body.scrollHeight > window.innerHeight || initialBodyOverflow === "scroll") {
    previousBodyPadding = parseInt(window.getComputedStyle(document.body).getPropertyValue("padding-right"));
    document.body.style.paddingRight = `${previousBodyPadding + measureScrollbar()}px`;
  }
};
var undoReplaceScrollbarWithPadding = () => {
  if (previousBodyPadding !== null) {
    document.body.style.paddingRight = `${previousBodyPadding}px`;
    previousBodyPadding = null;
  }
};
function removePopupAndResetState(instance, container, returnFocus, didClose) {
  if (isToast()) {
    triggerDidCloseAndDispose(instance, didClose);
  } else {
    restoreActiveElement(returnFocus).then(() => triggerDidCloseAndDispose(instance, didClose));
    removeKeydownHandler(globalState);
  }
  if (isSafariOrIOS) {
    container.setAttribute("style", "display:none !important");
    container.removeAttribute("class");
    container.innerHTML = "";
  } else {
    container.remove();
  }
  if (isModal()) {
    undoReplaceScrollbarWithPadding();
    undoIOSfix();
    unsetAriaHidden();
  }
  removeBodyClasses();
}
function removeBodyClasses() {
  removeClass([document.documentElement, document.body], [swalClasses.shown, swalClasses["height-auto"], swalClasses["no-backdrop"], swalClasses["toast-shown"]]);
}
function close(resolveValue) {
  resolveValue = prepareResolveValue(resolveValue);
  const swalPromiseResolve = privateMethods.swalPromiseResolve.get(this);
  const didClose = triggerClosePopup(this);
  if (this.isAwaitingPromise) {
    if (!resolveValue.isDismissed) {
      handleAwaitingPromise(this);
      swalPromiseResolve(resolveValue);
    }
  } else if (didClose) {
    swalPromiseResolve(resolveValue);
  }
}
var triggerClosePopup = (instance) => {
  const popup = getPopup();
  if (!popup) {
    return false;
  }
  const innerParams = privateProps.innerParams.get(instance);
  if (!innerParams || hasClass(popup, innerParams.hideClass.popup)) {
    return false;
  }
  removeClass(popup, innerParams.showClass.popup);
  addClass(popup, innerParams.hideClass.popup);
  const backdrop = getContainer();
  removeClass(backdrop, innerParams.showClass.backdrop);
  addClass(backdrop, innerParams.hideClass.backdrop);
  handlePopupAnimation(instance, popup, innerParams);
  return true;
};
function rejectPromise(error2) {
  const rejectPromise2 = privateMethods.swalPromiseReject.get(this);
  handleAwaitingPromise(this);
  if (rejectPromise2) {
    rejectPromise2(error2);
  }
}
var handleAwaitingPromise = (instance) => {
  if (instance.isAwaitingPromise) {
    delete instance.isAwaitingPromise;
    if (!privateProps.innerParams.get(instance)) {
      instance._destroy();
    }
  }
};
var prepareResolveValue = (resolveValue) => {
  if (typeof resolveValue === "undefined") {
    return {
      isConfirmed: false,
      isDenied: false,
      isDismissed: true
    };
  }
  return Object.assign({
    isConfirmed: false,
    isDenied: false,
    isDismissed: false
  }, resolveValue);
};
var handlePopupAnimation = (instance, popup, innerParams) => {
  var _globalState$eventEmi;
  const container = getContainer();
  const animationIsSupported = hasCssAnimation(popup);
  if (typeof innerParams.willClose === "function") {
    innerParams.willClose(popup);
  }
  (_globalState$eventEmi = globalState.eventEmitter) === null || _globalState$eventEmi === void 0 || _globalState$eventEmi.emit("willClose", popup);
  if (animationIsSupported) {
    animatePopup(instance, popup, container, innerParams.returnFocus, innerParams.didClose);
  } else {
    removePopupAndResetState(instance, container, innerParams.returnFocus, innerParams.didClose);
  }
};
var animatePopup = (instance, popup, container, returnFocus, didClose) => {
  globalState.swalCloseEventFinishedCallback = removePopupAndResetState.bind(null, instance, container, returnFocus, didClose);
  const swalCloseAnimationFinished = function(e) {
    if (e.target === popup) {
      var _globalState$swalClos;
      (_globalState$swalClos = globalState.swalCloseEventFinishedCallback) === null || _globalState$swalClos === void 0 || _globalState$swalClos.call(globalState);
      delete globalState.swalCloseEventFinishedCallback;
      popup.removeEventListener("animationend", swalCloseAnimationFinished);
      popup.removeEventListener("transitionend", swalCloseAnimationFinished);
    }
  };
  popup.addEventListener("animationend", swalCloseAnimationFinished);
  popup.addEventListener("transitionend", swalCloseAnimationFinished);
};
var triggerDidCloseAndDispose = (instance, didClose) => {
  setTimeout(() => {
    var _globalState$eventEmi2;
    if (typeof didClose === "function") {
      didClose.bind(instance.params)();
    }
    (_globalState$eventEmi2 = globalState.eventEmitter) === null || _globalState$eventEmi2 === void 0 || _globalState$eventEmi2.emit("didClose");
    if (instance._destroy) {
      instance._destroy();
    }
  });
};
var showLoading = (buttonToReplace) => {
  let popup = getPopup();
  if (!popup) {
    new Swal();
  }
  popup = getPopup();
  if (!popup) {
    return;
  }
  const loader = getLoader();
  if (isToast()) {
    hide(getIcon());
  } else {
    replaceButton(popup, buttonToReplace);
  }
  show(loader);
  popup.setAttribute("data-loading", "true");
  popup.setAttribute("aria-busy", "true");
  popup.focus();
};
var replaceButton = (popup, buttonToReplace) => {
  const actions = getActions();
  const loader = getLoader();
  if (!actions || !loader) {
    return;
  }
  if (!buttonToReplace && isVisible$1(getConfirmButton())) {
    buttonToReplace = getConfirmButton();
  }
  show(actions);
  if (buttonToReplace) {
    hide(buttonToReplace);
    loader.setAttribute("data-button-to-replace", buttonToReplace.className);
    actions.insertBefore(loader, buttonToReplace);
  }
  addClass([popup, actions], swalClasses.loading);
};
var handleInputOptionsAndValue = (instance, params) => {
  if (params.input === "select" || params.input === "radio") {
    handleInputOptions(instance, params);
  } else if (["text", "email", "number", "tel", "textarea"].some((i) => i === params.input) && (hasToPromiseFn(params.inputValue) || isPromise(params.inputValue))) {
    showLoading(getConfirmButton());
    handleInputValue(instance, params);
  }
};
var getInputValue = (instance, innerParams) => {
  const input = instance.getInput();
  if (!input) {
    return null;
  }
  switch (innerParams.input) {
    case "checkbox":
      return getCheckboxValue(input);
    case "radio":
      return getRadioValue(input);
    case "file":
      return getFileValue(input);
    default:
      return innerParams.inputAutoTrim ? input.value.trim() : input.value;
  }
};
var getCheckboxValue = (input) => input.checked ? 1 : 0;
var getRadioValue = (input) => input.checked ? input.value : null;
var getFileValue = (input) => input.files && input.files.length ? input.getAttribute("multiple") !== null ? input.files : input.files[0] : null;
var handleInputOptions = (instance, params) => {
  const popup = getPopup();
  if (!popup) {
    return;
  }
  const processInputOptions = (inputOptions) => {
    if (params.input === "select") {
      populateSelectOptions(popup, formatInputOptions(inputOptions), params);
    } else if (params.input === "radio") {
      populateRadioOptions(popup, formatInputOptions(inputOptions), params);
    }
  };
  if (hasToPromiseFn(params.inputOptions) || isPromise(params.inputOptions)) {
    showLoading(getConfirmButton());
    asPromise(params.inputOptions).then((inputOptions) => {
      instance.hideLoading();
      processInputOptions(inputOptions);
    });
  } else if (typeof params.inputOptions === "object") {
    processInputOptions(params.inputOptions);
  } else {
    error(`Unexpected type of inputOptions! Expected object, Map or Promise, got ${typeof params.inputOptions}`);
  }
};
var handleInputValue = (instance, params) => {
  const input = instance.getInput();
  if (!input) {
    return;
  }
  hide(input);
  asPromise(params.inputValue).then((inputValue) => {
    input.value = params.input === "number" ? `${parseFloat(inputValue) || 0}` : `${inputValue}`;
    show(input);
    input.focus();
    instance.hideLoading();
  }).catch((err) => {
    error(`Error in inputValue promise: ${err}`);
    input.value = "";
    show(input);
    input.focus();
    instance.hideLoading();
  });
};
function populateSelectOptions(popup, inputOptions, params) {
  const select = getDirectChildByClass(popup, swalClasses.select);
  if (!select) {
    return;
  }
  const renderOption = (parent, optionLabel, optionValue) => {
    const option = document.createElement("option");
    option.value = optionValue;
    setInnerHtml(option, optionLabel);
    option.selected = isSelected(optionValue, params.inputValue);
    parent.appendChild(option);
  };
  inputOptions.forEach((inputOption) => {
    const optionValue = inputOption[0];
    const optionLabel = inputOption[1];
    if (Array.isArray(optionLabel)) {
      const optgroup = document.createElement("optgroup");
      optgroup.label = optionValue;
      optgroup.disabled = false;
      select.appendChild(optgroup);
      optionLabel.forEach((o) => renderOption(optgroup, o[1], o[0]));
    } else {
      renderOption(select, optionLabel, optionValue);
    }
  });
  select.focus();
}
function populateRadioOptions(popup, inputOptions, params) {
  const radio = getDirectChildByClass(popup, swalClasses.radio);
  if (!radio) {
    return;
  }
  inputOptions.forEach((inputOption) => {
    const radioValue = inputOption[0];
    const radioLabel = inputOption[1];
    const radioInput = document.createElement("input");
    const radioLabelElement = document.createElement("label");
    radioInput.type = "radio";
    radioInput.name = swalClasses.radio;
    radioInput.value = radioValue;
    if (isSelected(radioValue, params.inputValue)) {
      radioInput.checked = true;
    }
    const label = document.createElement("span");
    setInnerHtml(label, radioLabel);
    label.className = swalClasses.label;
    radioLabelElement.appendChild(radioInput);
    radioLabelElement.appendChild(label);
    radio.appendChild(radioLabelElement);
  });
  const radios = radio.querySelectorAll("input");
  if (radios.length) {
    radios[0].focus();
  }
}
var formatInputOptions = (inputOptions) => {
  const result = [];
  if (inputOptions instanceof Map) {
    inputOptions.forEach((value, key) => {
      let valueFormatted = value;
      if (typeof valueFormatted === "object") {
        valueFormatted = formatInputOptions(valueFormatted);
      }
      result.push([key, valueFormatted]);
    });
  } else {
    Object.keys(inputOptions).forEach((key) => {
      let valueFormatted = inputOptions[key];
      if (typeof valueFormatted === "object") {
        valueFormatted = formatInputOptions(valueFormatted);
      }
      result.push([key, valueFormatted]);
    });
  }
  return result;
};
var isSelected = (optionValue, inputValue) => {
  return !!inputValue && inputValue.toString() === optionValue.toString();
};
var handleConfirmButtonClick = (instance) => {
  const innerParams = privateProps.innerParams.get(instance);
  instance.disableButtons();
  if (innerParams.input) {
    handleConfirmOrDenyWithInput(instance, "confirm");
  } else {
    confirm(instance, true);
  }
};
var handleDenyButtonClick = (instance) => {
  const innerParams = privateProps.innerParams.get(instance);
  instance.disableButtons();
  if (innerParams.returnInputValueOnDeny) {
    handleConfirmOrDenyWithInput(instance, "deny");
  } else {
    deny(instance, false);
  }
};
var handleCancelButtonClick = (instance, dismissWith) => {
  instance.disableButtons();
  dismissWith(DismissReason.cancel);
};
var handleConfirmOrDenyWithInput = (instance, type) => {
  const innerParams = privateProps.innerParams.get(instance);
  if (!innerParams.input) {
    error(`The "input" parameter is needed to be set when using returnInputValueOn${capitalizeFirstLetter(type)}`);
    return;
  }
  const input = instance.getInput();
  const inputValue = getInputValue(instance, innerParams);
  if (innerParams.inputValidator) {
    handleInputValidator(instance, inputValue, type);
  } else if (input && !input.checkValidity()) {
    instance.enableButtons();
    instance.showValidationMessage(innerParams.validationMessage || input.validationMessage);
  } else if (type === "deny") {
    deny(instance, inputValue);
  } else {
    confirm(instance, inputValue);
  }
};
var handleInputValidator = (instance, inputValue, type) => {
  const innerParams = privateProps.innerParams.get(instance);
  instance.disableInput();
  const validationPromise = Promise.resolve().then(() => asPromise(innerParams.inputValidator(inputValue, innerParams.validationMessage)));
  validationPromise.then((validationMessage) => {
    instance.enableButtons();
    instance.enableInput();
    if (validationMessage) {
      instance.showValidationMessage(validationMessage);
    } else if (type === "deny") {
      deny(instance, inputValue);
    } else {
      confirm(instance, inputValue);
    }
  });
};
var deny = (instance, value) => {
  const innerParams = privateProps.innerParams.get(instance || void 0);
  if (innerParams.showLoaderOnDeny) {
    showLoading(getDenyButton());
  }
  if (innerParams.preDeny) {
    instance.isAwaitingPromise = true;
    const preDenyPromise = Promise.resolve().then(() => asPromise(innerParams.preDeny(value, innerParams.validationMessage)));
    preDenyPromise.then((preDenyValue) => {
      if (preDenyValue === false) {
        instance.hideLoading();
        handleAwaitingPromise(instance);
      } else {
        instance.close({
          isDenied: true,
          value: typeof preDenyValue === "undefined" ? value : preDenyValue
        });
      }
    }).catch((error2) => rejectWith(instance || void 0, error2));
  } else {
    instance.close({
      isDenied: true,
      value
    });
  }
};
var succeedWith = (instance, value) => {
  instance.close({
    isConfirmed: true,
    value
  });
};
var rejectWith = (instance, error2) => {
  instance.rejectPromise(error2);
};
var confirm = (instance, value) => {
  const innerParams = privateProps.innerParams.get(instance || void 0);
  if (innerParams.showLoaderOnConfirm) {
    showLoading();
  }
  if (innerParams.preConfirm) {
    instance.resetValidationMessage();
    instance.isAwaitingPromise = true;
    const preConfirmPromise = Promise.resolve().then(() => asPromise(innerParams.preConfirm(value, innerParams.validationMessage)));
    preConfirmPromise.then((preConfirmValue) => {
      if (isVisible$1(getValidationMessage()) || preConfirmValue === false) {
        instance.hideLoading();
        handleAwaitingPromise(instance);
      } else {
        succeedWith(instance, typeof preConfirmValue === "undefined" ? value : preConfirmValue);
      }
    }).catch((error2) => rejectWith(instance || void 0, error2));
  } else {
    succeedWith(instance, value);
  }
};
function hideLoading() {
  const innerParams = privateProps.innerParams.get(this);
  if (!innerParams) {
    return;
  }
  const domCache = privateProps.domCache.get(this);
  hide(domCache.loader);
  if (isToast()) {
    if (innerParams.icon) {
      show(getIcon());
    }
  } else {
    showRelatedButton(domCache);
  }
  removeClass([domCache.popup, domCache.actions], swalClasses.loading);
  domCache.popup.removeAttribute("aria-busy");
  domCache.popup.removeAttribute("data-loading");
  domCache.confirmButton.disabled = false;
  domCache.denyButton.disabled = false;
  domCache.cancelButton.disabled = false;
}
var showRelatedButton = (domCache) => {
  const buttonToReplace = domCache.popup.getElementsByClassName(domCache.loader.getAttribute("data-button-to-replace"));
  if (buttonToReplace.length) {
    show(buttonToReplace[0], "inline-block");
  } else if (allButtonsAreHidden()) {
    hide(domCache.actions);
  }
};
function getInput() {
  const innerParams = privateProps.innerParams.get(this);
  const domCache = privateProps.domCache.get(this);
  if (!domCache) {
    return null;
  }
  return getInput$1(domCache.popup, innerParams.input);
}
function setButtonsDisabled(instance, buttons, disabled) {
  const domCache = privateProps.domCache.get(instance);
  buttons.forEach((button) => {
    domCache[button].disabled = disabled;
  });
}
function setInputDisabled(input, disabled) {
  const popup = getPopup();
  if (!popup || !input) {
    return;
  }
  if (input.type === "radio") {
    const radios = popup.querySelectorAll(`[name="${swalClasses.radio}"]`);
    for (let i = 0; i < radios.length; i++) {
      radios[i].disabled = disabled;
    }
  } else {
    input.disabled = disabled;
  }
}
function enableButtons() {
  setButtonsDisabled(this, ["confirmButton", "denyButton", "cancelButton"], false);
}
function disableButtons() {
  setButtonsDisabled(this, ["confirmButton", "denyButton", "cancelButton"], true);
}
function enableInput() {
  setInputDisabled(this.getInput(), false);
}
function disableInput() {
  setInputDisabled(this.getInput(), true);
}
function showValidationMessage(error2) {
  const domCache = privateProps.domCache.get(this);
  const params = privateProps.innerParams.get(this);
  setInnerHtml(domCache.validationMessage, error2);
  domCache.validationMessage.className = swalClasses["validation-message"];
  if (params.customClass && params.customClass.validationMessage) {
    addClass(domCache.validationMessage, params.customClass.validationMessage);
  }
  show(domCache.validationMessage);
  const input = this.getInput();
  if (input) {
    input.setAttribute("aria-invalid", "true");
    input.setAttribute("aria-describedby", swalClasses["validation-message"]);
    focusInput(input);
    addClass(input, swalClasses.inputerror);
  }
}
function resetValidationMessage() {
  const domCache = privateProps.domCache.get(this);
  if (domCache.validationMessage) {
    hide(domCache.validationMessage);
  }
  const input = this.getInput();
  if (input) {
    input.removeAttribute("aria-invalid");
    input.removeAttribute("aria-describedby");
    removeClass(input, swalClasses.inputerror);
  }
}
var defaultParams = {
  title: "",
  titleText: "",
  text: "",
  html: "",
  footer: "",
  icon: void 0,
  iconColor: void 0,
  iconHtml: void 0,
  template: void 0,
  toast: false,
  draggable: false,
  animation: true,
  theme: "light",
  showClass: {
    popup: "swal2-show",
    backdrop: "swal2-backdrop-show",
    icon: "swal2-icon-show"
  },
  hideClass: {
    popup: "swal2-hide",
    backdrop: "swal2-backdrop-hide",
    icon: "swal2-icon-hide"
  },
  customClass: {},
  target: "body",
  color: void 0,
  backdrop: true,
  heightAuto: true,
  allowOutsideClick: true,
  allowEscapeKey: true,
  allowEnterKey: true,
  stopKeydownPropagation: true,
  keydownListenerCapture: false,
  showConfirmButton: true,
  showDenyButton: false,
  showCancelButton: false,
  preConfirm: void 0,
  preDeny: void 0,
  confirmButtonText: "OK",
  confirmButtonAriaLabel: "",
  confirmButtonColor: void 0,
  denyButtonText: "No",
  denyButtonAriaLabel: "",
  denyButtonColor: void 0,
  cancelButtonText: "Cancel",
  cancelButtonAriaLabel: "",
  cancelButtonColor: void 0,
  buttonsStyling: true,
  reverseButtons: false,
  focusConfirm: true,
  focusDeny: false,
  focusCancel: false,
  returnFocus: true,
  showCloseButton: false,
  closeButtonHtml: "&times;",
  closeButtonAriaLabel: "Close this dialog",
  loaderHtml: "",
  showLoaderOnConfirm: false,
  showLoaderOnDeny: false,
  imageUrl: void 0,
  imageWidth: void 0,
  imageHeight: void 0,
  imageAlt: "",
  timer: void 0,
  timerProgressBar: false,
  width: void 0,
  padding: void 0,
  background: void 0,
  input: void 0,
  inputPlaceholder: "",
  inputLabel: "",
  inputValue: "",
  inputOptions: {},
  inputAutoFocus: true,
  inputAutoTrim: true,
  inputAttributes: {},
  inputValidator: void 0,
  returnInputValueOnDeny: false,
  validationMessage: void 0,
  grow: false,
  position: "center",
  progressSteps: [],
  currentProgressStep: void 0,
  progressStepsDistance: void 0,
  willOpen: void 0,
  didOpen: void 0,
  didRender: void 0,
  willClose: void 0,
  didClose: void 0,
  didDestroy: void 0,
  scrollbarPadding: true,
  topLayer: false
};
var updatableParams = ["allowEscapeKey", "allowOutsideClick", "background", "buttonsStyling", "cancelButtonAriaLabel", "cancelButtonColor", "cancelButtonText", "closeButtonAriaLabel", "closeButtonHtml", "color", "confirmButtonAriaLabel", "confirmButtonColor", "confirmButtonText", "currentProgressStep", "customClass", "denyButtonAriaLabel", "denyButtonColor", "denyButtonText", "didClose", "didDestroy", "draggable", "footer", "hideClass", "html", "icon", "iconColor", "iconHtml", "imageAlt", "imageHeight", "imageUrl", "imageWidth", "preConfirm", "preDeny", "progressSteps", "returnFocus", "reverseButtons", "showCancelButton", "showCloseButton", "showConfirmButton", "showDenyButton", "text", "title", "titleText", "theme", "willClose"];
var deprecatedParams = {
  allowEnterKey: void 0
};
var toastIncompatibleParams = ["allowOutsideClick", "allowEnterKey", "backdrop", "draggable", "focusConfirm", "focusDeny", "focusCancel", "returnFocus", "heightAuto", "keydownListenerCapture"];
var isValidParameter = (paramName) => {
  return Object.prototype.hasOwnProperty.call(defaultParams, paramName);
};
var isUpdatableParameter = (paramName) => {
  return updatableParams.indexOf(paramName) !== -1;
};
var isDeprecatedParameter = (paramName) => {
  return deprecatedParams[paramName];
};
var checkIfParamIsValid = (param) => {
  if (!isValidParameter(param)) {
    warn(`Unknown parameter "${param}"`);
  }
};
var checkIfToastParamIsValid = (param) => {
  if (toastIncompatibleParams.includes(param)) {
    warn(`The parameter "${param}" is incompatible with toasts`);
  }
};
var checkIfParamIsDeprecated = (param) => {
  const isDeprecated = isDeprecatedParameter(param);
  if (isDeprecated) {
    warnAboutDeprecation(param, isDeprecated);
  }
};
var showWarningsForParams = (params) => {
  if (params.backdrop === false && params.allowOutsideClick) {
    warn('"allowOutsideClick" parameter requires `backdrop` parameter to be set to `true`');
  }
  if (params.theme && !["light", "dark", "auto", "minimal", "borderless", "embed-iframe", "bulma", "bulma-light", "bulma-dark"].includes(params.theme)) {
    warn(`Invalid theme "${params.theme}"`);
  }
  for (const param in params) {
    checkIfParamIsValid(param);
    if (params.toast) {
      checkIfToastParamIsValid(param);
    }
    checkIfParamIsDeprecated(param);
  }
};
function update(params) {
  const container = getContainer();
  const popup = getPopup();
  const innerParams = privateProps.innerParams.get(this);
  if (!popup || hasClass(popup, innerParams.hideClass.popup)) {
    warn(`You're trying to update the closed or closing popup, that won't work. Use the update() method in preConfirm parameter or show a new popup.`);
    return;
  }
  const validUpdatableParams = filterValidParams(params);
  const updatedParams = Object.assign({}, innerParams, validUpdatableParams);
  showWarningsForParams(updatedParams);
  container.dataset["swal2Theme"] = updatedParams.theme;
  render(this, updatedParams);
  privateProps.innerParams.set(this, updatedParams);
  Object.defineProperties(this, {
    params: {
      value: Object.assign({}, this.params, params),
      writable: false,
      enumerable: true
    }
  });
}
var filterValidParams = (params) => {
  const validUpdatableParams = {};
  Object.keys(params).forEach((param) => {
    if (isUpdatableParameter(param)) {
      validUpdatableParams[param] = params[param];
    } else {
      warn(`Invalid parameter to update: ${param}`);
    }
  });
  return validUpdatableParams;
};
function _destroy() {
  const domCache = privateProps.domCache.get(this);
  const innerParams = privateProps.innerParams.get(this);
  if (!innerParams) {
    disposeWeakMaps(this);
    return;
  }
  if (domCache.popup && globalState.swalCloseEventFinishedCallback) {
    globalState.swalCloseEventFinishedCallback();
    delete globalState.swalCloseEventFinishedCallback;
  }
  if (typeof innerParams.didDestroy === "function") {
    innerParams.didDestroy();
  }
  globalState.eventEmitter.emit("didDestroy");
  disposeSwal(this);
}
var disposeSwal = (instance) => {
  disposeWeakMaps(instance);
  delete instance.params;
  delete globalState.keydownHandler;
  delete globalState.keydownTarget;
  delete globalState.currentInstance;
};
var disposeWeakMaps = (instance) => {
  if (instance.isAwaitingPromise) {
    unsetWeakMaps(privateProps, instance);
    instance.isAwaitingPromise = true;
  } else {
    unsetWeakMaps(privateMethods, instance);
    unsetWeakMaps(privateProps, instance);
    delete instance.isAwaitingPromise;
    delete instance.disableButtons;
    delete instance.enableButtons;
    delete instance.getInput;
    delete instance.disableInput;
    delete instance.enableInput;
    delete instance.hideLoading;
    delete instance.disableLoading;
    delete instance.showValidationMessage;
    delete instance.resetValidationMessage;
    delete instance.close;
    delete instance.closePopup;
    delete instance.closeModal;
    delete instance.closeToast;
    delete instance.rejectPromise;
    delete instance.update;
    delete instance._destroy;
  }
};
var unsetWeakMaps = (obj, instance) => {
  for (const i in obj) {
    obj[i].delete(instance);
  }
};
var instanceMethods = Object.freeze({
  __proto__: null,
  _destroy,
  close,
  closeModal: close,
  closePopup: close,
  closeToast: close,
  disableButtons,
  disableInput,
  disableLoading: hideLoading,
  enableButtons,
  enableInput,
  getInput,
  handleAwaitingPromise,
  hideLoading,
  rejectPromise,
  resetValidationMessage,
  showValidationMessage,
  update
});
var handlePopupClick = (innerParams, domCache, dismissWith) => {
  if (innerParams.toast) {
    handleToastClick(innerParams, domCache, dismissWith);
  } else {
    handleModalMousedown(domCache);
    handleContainerMousedown(domCache);
    handleModalClick(innerParams, domCache, dismissWith);
  }
};
var handleToastClick = (innerParams, domCache, dismissWith) => {
  domCache.popup.onclick = () => {
    if (innerParams && (isAnyButtonShown(innerParams) || innerParams.timer || innerParams.input)) {
      return;
    }
    dismissWith(DismissReason.close);
  };
};
var isAnyButtonShown = (innerParams) => {
  return !!(innerParams.showConfirmButton || innerParams.showDenyButton || innerParams.showCancelButton || innerParams.showCloseButton);
};
var ignoreOutsideClick = false;
var handleModalMousedown = (domCache) => {
  domCache.popup.onmousedown = () => {
    domCache.container.onmouseup = function(e) {
      domCache.container.onmouseup = () => {
      };
      if (e.target === domCache.container) {
        ignoreOutsideClick = true;
      }
    };
  };
};
var handleContainerMousedown = (domCache) => {
  domCache.container.onmousedown = (e) => {
    if (e.target === domCache.container) {
      e.preventDefault();
    }
    domCache.popup.onmouseup = function(e2) {
      domCache.popup.onmouseup = () => {
      };
      if (e2.target === domCache.popup || e2.target instanceof HTMLElement && domCache.popup.contains(e2.target)) {
        ignoreOutsideClick = true;
      }
    };
  };
};
var handleModalClick = (innerParams, domCache, dismissWith) => {
  domCache.container.onclick = (e) => {
    if (ignoreOutsideClick) {
      ignoreOutsideClick = false;
      return;
    }
    if (e.target === domCache.container && callIfFunction(innerParams.allowOutsideClick)) {
      dismissWith(DismissReason.backdrop);
    }
  };
};
var isJqueryElement = (elem) => typeof elem === "object" && elem.jquery;
var isElement = (elem) => elem instanceof Element || isJqueryElement(elem);
var argsToParams = (args) => {
  const params = {};
  if (typeof args[0] === "object" && !isElement(args[0])) {
    Object.assign(params, args[0]);
  } else {
    ["title", "html", "icon"].forEach((name, index) => {
      const arg = args[index];
      if (typeof arg === "string" || isElement(arg)) {
        params[name] = arg;
      } else if (arg !== void 0) {
        error(`Unexpected type of ${name}! Expected "string" or "Element", got ${typeof arg}`);
      }
    });
  }
  return params;
};
function fire(...args) {
  return new this(...args);
}
function mixin(mixinParams) {
  class MixinSwal extends this {
    _main(params, priorityMixinParams) {
      return super._main(params, Object.assign({}, mixinParams, priorityMixinParams));
    }
  }
  return MixinSwal;
}
var getTimerLeft = () => {
  return globalState.timeout && globalState.timeout.getTimerLeft();
};
var stopTimer = () => {
  if (globalState.timeout) {
    stopTimerProgressBar();
    return globalState.timeout.stop();
  }
};
var resumeTimer = () => {
  if (globalState.timeout) {
    const remaining = globalState.timeout.start();
    animateTimerProgressBar(remaining);
    return remaining;
  }
};
var toggleTimer = () => {
  const timer = globalState.timeout;
  return timer && (timer.running ? stopTimer() : resumeTimer());
};
var increaseTimer = (ms) => {
  if (globalState.timeout) {
    const remaining = globalState.timeout.increase(ms);
    animateTimerProgressBar(remaining, true);
    return remaining;
  }
};
var isTimerRunning = () => {
  return !!(globalState.timeout && globalState.timeout.isRunning());
};
var bodyClickListenerAdded = false;
var clickHandlers = {};
function bindClickHandler(attr = "data-swal-template") {
  clickHandlers[attr] = this;
  if (!bodyClickListenerAdded) {
    document.body.addEventListener("click", bodyClickListener);
    bodyClickListenerAdded = true;
  }
}
var bodyClickListener = (event) => {
  for (let el = event.target; el && el !== document; el = el.parentNode) {
    for (const attr in clickHandlers) {
      const template = el.getAttribute(attr);
      if (template) {
        clickHandlers[attr].fire({
          template
        });
        return;
      }
    }
  }
};
var EventEmitter = class {
  constructor() {
    this.events = {};
  }
  /**
   * @param {string} eventName
   * @returns {EventHandlers}
   */
  _getHandlersByEventName(eventName) {
    if (typeof this.events[eventName] === "undefined") {
      this.events[eventName] = [];
    }
    return this.events[eventName];
  }
  /**
   * @param {string} eventName
   * @param {EventHandler} eventHandler
   */
  on(eventName, eventHandler) {
    const currentHandlers = this._getHandlersByEventName(eventName);
    if (!currentHandlers.includes(eventHandler)) {
      currentHandlers.push(eventHandler);
    }
  }
  /**
   * @param {string} eventName
   * @param {EventHandler} eventHandler
   */
  once(eventName, eventHandler) {
    const onceFn = (...args) => {
      this.removeListener(eventName, onceFn);
      eventHandler.apply(this, args);
    };
    this.on(eventName, onceFn);
  }
  /**
   * @param {string} eventName
   * @param {Array} args
   */
  emit(eventName, ...args) {
    this._getHandlersByEventName(eventName).forEach(
      /**
       * @param {EventHandler} eventHandler
       */
      (eventHandler) => {
        try {
          eventHandler.apply(this, args);
        } catch (error2) {
          console.error(error2);
        }
      }
    );
  }
  /**
   * @param {string} eventName
   * @param {EventHandler} eventHandler
   */
  removeListener(eventName, eventHandler) {
    const currentHandlers = this._getHandlersByEventName(eventName);
    const index = currentHandlers.indexOf(eventHandler);
    if (index > -1) {
      currentHandlers.splice(index, 1);
    }
  }
  /**
   * @param {string} eventName
   */
  removeAllListeners(eventName) {
    if (this.events[eventName] !== void 0) {
      this.events[eventName].length = 0;
    }
  }
  reset() {
    this.events = {};
  }
};
globalState.eventEmitter = new EventEmitter();
var on = (eventName, eventHandler) => {
  globalState.eventEmitter.on(eventName, eventHandler);
};
var once = (eventName, eventHandler) => {
  globalState.eventEmitter.once(eventName, eventHandler);
};
var off = (eventName, eventHandler) => {
  if (!eventName) {
    globalState.eventEmitter.reset();
    return;
  }
  if (eventHandler) {
    globalState.eventEmitter.removeListener(eventName, eventHandler);
  } else {
    globalState.eventEmitter.removeAllListeners(eventName);
  }
};
var staticMethods = Object.freeze({
  __proto__: null,
  argsToParams,
  bindClickHandler,
  clickCancel,
  clickConfirm,
  clickDeny,
  enableLoading: showLoading,
  fire,
  getActions,
  getCancelButton,
  getCloseButton,
  getConfirmButton,
  getContainer,
  getDenyButton,
  getFocusableElements,
  getFooter,
  getHtmlContainer,
  getIcon,
  getIconContent,
  getImage,
  getInputLabel,
  getLoader,
  getPopup,
  getProgressSteps,
  getTimerLeft,
  getTimerProgressBar,
  getTitle,
  getValidationMessage,
  increaseTimer,
  isDeprecatedParameter,
  isLoading,
  isTimerRunning,
  isUpdatableParameter,
  isValidParameter,
  isVisible,
  mixin,
  off,
  on,
  once,
  resumeTimer,
  showLoading,
  stopTimer,
  toggleTimer
});
var Timer = class {
  /**
   * @param {Function} callback
   * @param {number} delay
   */
  constructor(callback, delay) {
    this.callback = callback;
    this.remaining = delay;
    this.running = false;
    this.start();
  }
  /**
   * @returns {number}
   */
  start() {
    if (!this.running) {
      this.running = true;
      this.started = /* @__PURE__ */ new Date();
      this.id = setTimeout(this.callback, this.remaining);
    }
    return this.remaining;
  }
  /**
   * @returns {number}
   */
  stop() {
    if (this.started && this.running) {
      this.running = false;
      clearTimeout(this.id);
      this.remaining -= (/* @__PURE__ */ new Date()).getTime() - this.started.getTime();
    }
    return this.remaining;
  }
  /**
   * @param {number} n
   * @returns {number}
   */
  increase(n) {
    const running = this.running;
    if (running) {
      this.stop();
    }
    this.remaining += n;
    if (running) {
      this.start();
    }
    return this.remaining;
  }
  /**
   * @returns {number}
   */
  getTimerLeft() {
    if (this.running) {
      this.stop();
      this.start();
    }
    return this.remaining;
  }
  /**
   * @returns {boolean}
   */
  isRunning() {
    return this.running;
  }
};
var swalStringParams = ["swal-title", "swal-html", "swal-footer"];
var getTemplateParams = (params) => {
  const template = typeof params.template === "string" ? (
    /** @type {HTMLTemplateElement} */
    document.querySelector(params.template)
  ) : params.template;
  if (!template) {
    return {};
  }
  const templateContent = template.content;
  showWarningsForElements(templateContent);
  const result = Object.assign(getSwalParams(templateContent), getSwalFunctionParams(templateContent), getSwalButtons(templateContent), getSwalImage(templateContent), getSwalIcon(templateContent), getSwalInput(templateContent), getSwalStringParams(templateContent, swalStringParams));
  return result;
};
var getSwalParams = (templateContent) => {
  const result = {};
  const swalParams = Array.from(templateContent.querySelectorAll("swal-param"));
  swalParams.forEach((param) => {
    showWarningsForAttributes(param, ["name", "value"]);
    const paramName = (
      /** @type {keyof SweetAlertOptions} */
      param.getAttribute("name")
    );
    const value = param.getAttribute("value");
    if (!paramName || !value) {
      return;
    }
    if (typeof defaultParams[paramName] === "boolean") {
      result[paramName] = value !== "false";
    } else if (typeof defaultParams[paramName] === "object") {
      result[paramName] = JSON.parse(value);
    } else {
      result[paramName] = value;
    }
  });
  return result;
};
var getSwalFunctionParams = (templateContent) => {
  const result = {};
  const swalFunctions = Array.from(templateContent.querySelectorAll("swal-function-param"));
  swalFunctions.forEach((param) => {
    const paramName = (
      /** @type {keyof SweetAlertOptions} */
      param.getAttribute("name")
    );
    const value = param.getAttribute("value");
    if (!paramName || !value) {
      return;
    }
    result[paramName] = new Function(`return ${value}`)();
  });
  return result;
};
var getSwalButtons = (templateContent) => {
  const result = {};
  const swalButtons = Array.from(templateContent.querySelectorAll("swal-button"));
  swalButtons.forEach((button) => {
    showWarningsForAttributes(button, ["type", "color", "aria-label"]);
    const type = button.getAttribute("type");
    if (!type || !["confirm", "cancel", "deny"].includes(type)) {
      return;
    }
    result[`${type}ButtonText`] = button.innerHTML;
    result[`show${capitalizeFirstLetter(type)}Button`] = true;
    if (button.hasAttribute("color")) {
      result[`${type}ButtonColor`] = button.getAttribute("color");
    }
    if (button.hasAttribute("aria-label")) {
      result[`${type}ButtonAriaLabel`] = button.getAttribute("aria-label");
    }
  });
  return result;
};
var getSwalImage = (templateContent) => {
  const result = {};
  const image = templateContent.querySelector("swal-image");
  if (image) {
    showWarningsForAttributes(image, ["src", "width", "height", "alt"]);
    if (image.hasAttribute("src")) {
      result.imageUrl = image.getAttribute("src") || void 0;
    }
    if (image.hasAttribute("width")) {
      result.imageWidth = image.getAttribute("width") || void 0;
    }
    if (image.hasAttribute("height")) {
      result.imageHeight = image.getAttribute("height") || void 0;
    }
    if (image.hasAttribute("alt")) {
      result.imageAlt = image.getAttribute("alt") || void 0;
    }
  }
  return result;
};
var getSwalIcon = (templateContent) => {
  const result = {};
  const icon = templateContent.querySelector("swal-icon");
  if (icon) {
    showWarningsForAttributes(icon, ["type", "color"]);
    if (icon.hasAttribute("type")) {
      result.icon = icon.getAttribute("type");
    }
    if (icon.hasAttribute("color")) {
      result.iconColor = icon.getAttribute("color");
    }
    result.iconHtml = icon.innerHTML;
  }
  return result;
};
var getSwalInput = (templateContent) => {
  const result = {};
  const input = templateContent.querySelector("swal-input");
  if (input) {
    showWarningsForAttributes(input, ["type", "label", "placeholder", "value"]);
    result.input = input.getAttribute("type") || "text";
    if (input.hasAttribute("label")) {
      result.inputLabel = input.getAttribute("label");
    }
    if (input.hasAttribute("placeholder")) {
      result.inputPlaceholder = input.getAttribute("placeholder");
    }
    if (input.hasAttribute("value")) {
      result.inputValue = input.getAttribute("value");
    }
  }
  const inputOptions = Array.from(templateContent.querySelectorAll("swal-input-option"));
  if (inputOptions.length) {
    result.inputOptions = {};
    inputOptions.forEach((option) => {
      showWarningsForAttributes(option, ["value"]);
      const optionValue = option.getAttribute("value");
      if (!optionValue) {
        return;
      }
      const optionName = option.innerHTML;
      result.inputOptions[optionValue] = optionName;
    });
  }
  return result;
};
var getSwalStringParams = (templateContent, paramNames) => {
  const result = {};
  for (const i in paramNames) {
    const paramName = paramNames[i];
    const tag = templateContent.querySelector(paramName);
    if (tag) {
      showWarningsForAttributes(tag, []);
      result[paramName.replace(/^swal-/, "")] = tag.innerHTML.trim();
    }
  }
  return result;
};
var showWarningsForElements = (templateContent) => {
  const allowedElements = swalStringParams.concat(["swal-param", "swal-function-param", "swal-button", "swal-image", "swal-icon", "swal-input", "swal-input-option"]);
  Array.from(templateContent.children).forEach((el) => {
    const tagName = el.tagName.toLowerCase();
    if (!allowedElements.includes(tagName)) {
      warn(`Unrecognized element <${tagName}>`);
    }
  });
};
var showWarningsForAttributes = (el, allowedAttributes) => {
  Array.from(el.attributes).forEach((attribute) => {
    if (allowedAttributes.indexOf(attribute.name) === -1) {
      warn([`Unrecognized attribute "${attribute.name}" on <${el.tagName.toLowerCase()}>.`, `${allowedAttributes.length ? `Allowed attributes are: ${allowedAttributes.join(", ")}` : "To set the value, use HTML within the element."}`]);
    }
  });
};
var SHOW_CLASS_TIMEOUT = 10;
var openPopup = (params) => {
  const container = getContainer();
  const popup = getPopup();
  if (typeof params.willOpen === "function") {
    params.willOpen(popup);
  }
  globalState.eventEmitter.emit("willOpen", popup);
  const bodyStyles = window.getComputedStyle(document.body);
  const initialBodyOverflow = bodyStyles.overflowY;
  addClasses(container, popup, params);
  setTimeout(() => {
    setScrollingVisibility(container, popup);
  }, SHOW_CLASS_TIMEOUT);
  if (isModal()) {
    fixScrollContainer(container, params.scrollbarPadding, initialBodyOverflow);
    setAriaHidden();
  }
  if (!isToast() && !globalState.previousActiveElement) {
    globalState.previousActiveElement = document.activeElement;
  }
  if (typeof params.didOpen === "function") {
    setTimeout(() => params.didOpen(popup));
  }
  globalState.eventEmitter.emit("didOpen", popup);
  removeClass(container, swalClasses["no-transition"]);
};
var swalOpenAnimationFinished = (event) => {
  const popup = getPopup();
  if (event.target !== popup) {
    return;
  }
  const container = getContainer();
  popup.removeEventListener("animationend", swalOpenAnimationFinished);
  popup.removeEventListener("transitionend", swalOpenAnimationFinished);
  container.style.overflowY = "auto";
};
var setScrollingVisibility = (container, popup) => {
  if (hasCssAnimation(popup)) {
    container.style.overflowY = "hidden";
    popup.addEventListener("animationend", swalOpenAnimationFinished);
    popup.addEventListener("transitionend", swalOpenAnimationFinished);
  } else {
    container.style.overflowY = "auto";
  }
};
var fixScrollContainer = (container, scrollbarPadding, initialBodyOverflow) => {
  iOSfix();
  if (scrollbarPadding && initialBodyOverflow !== "hidden") {
    replaceScrollbarWithPadding(initialBodyOverflow);
  }
  setTimeout(() => {
    container.scrollTop = 0;
  });
};
var addClasses = (container, popup, params) => {
  addClass(container, params.showClass.backdrop);
  if (params.animation) {
    popup.style.setProperty("opacity", "0", "important");
    show(popup, "grid");
    setTimeout(() => {
      addClass(popup, params.showClass.popup);
      popup.style.removeProperty("opacity");
    }, SHOW_CLASS_TIMEOUT);
  } else {
    show(popup, "grid");
  }
  addClass([document.documentElement, document.body], swalClasses.shown);
  if (params.heightAuto && params.backdrop && !params.toast) {
    addClass([document.documentElement, document.body], swalClasses["height-auto"]);
  }
};
var defaultInputValidators = {
  /**
   * @param {string} string
   * @param {string} [validationMessage]
   * @returns {Promise<string | void>}
   */
  email: (string, validationMessage) => {
    return /^[a-zA-Z0-9.+_'-]+@[a-zA-Z0-9.-]+\.[a-zA-Z0-9-]+$/.test(string) ? Promise.resolve() : Promise.resolve(validationMessage || "Invalid email address");
  },
  /**
   * @param {string} string
   * @param {string} [validationMessage]
   * @returns {Promise<string | void>}
   */
  url: (string, validationMessage) => {
    return /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-z]{2,63}\b([-a-zA-Z0-9@:%_+.~#?&/=]*)$/.test(string) ? Promise.resolve() : Promise.resolve(validationMessage || "Invalid URL");
  }
};
function setDefaultInputValidators(params) {
  if (params.inputValidator) {
    return;
  }
  if (params.input === "email") {
    params.inputValidator = defaultInputValidators["email"];
  }
  if (params.input === "url") {
    params.inputValidator = defaultInputValidators["url"];
  }
}
function validateCustomTargetElement(params) {
  if (!params.target || typeof params.target === "string" && !document.querySelector(params.target) || typeof params.target !== "string" && !params.target.appendChild) {
    warn('Target parameter is not valid, defaulting to "body"');
    params.target = "body";
  }
}
function setParameters(params) {
  setDefaultInputValidators(params);
  if (params.showLoaderOnConfirm && !params.preConfirm) {
    warn("showLoaderOnConfirm is set to true, but preConfirm is not defined.\nshowLoaderOnConfirm should be used together with preConfirm, see usage example:\nhttps://sweetalert2.github.io/#ajax-request");
  }
  validateCustomTargetElement(params);
  if (typeof params.title === "string") {
    params.title = params.title.split("\n").join("<br />");
  }
  init(params);
}
var currentInstance;
var _promise = /* @__PURE__ */ new WeakMap();
var SweetAlert = class {
  /**
   * @param {...any} args
   * @this {SweetAlert}
   */
  constructor(...args) {
    _classPrivateFieldInitSpec(this, _promise, void 0);
    if (typeof window === "undefined") {
      return;
    }
    currentInstance = this;
    const outerParams = Object.freeze(this.constructor.argsToParams(args));
    this.params = outerParams;
    this.isAwaitingPromise = false;
    _classPrivateFieldSet2(_promise, this, this._main(currentInstance.params));
  }
  _main(userParams, mixinParams = {}) {
    showWarningsForParams(Object.assign({}, mixinParams, userParams));
    if (globalState.currentInstance) {
      const swalPromiseResolve = privateMethods.swalPromiseResolve.get(globalState.currentInstance);
      const {
        isAwaitingPromise
      } = globalState.currentInstance;
      globalState.currentInstance._destroy();
      if (!isAwaitingPromise) {
        swalPromiseResolve({
          isDismissed: true
        });
      }
      if (isModal()) {
        unsetAriaHidden();
      }
    }
    globalState.currentInstance = currentInstance;
    const innerParams = prepareParams(userParams, mixinParams);
    setParameters(innerParams);
    Object.freeze(innerParams);
    if (globalState.timeout) {
      globalState.timeout.stop();
      delete globalState.timeout;
    }
    clearTimeout(globalState.restoreFocusTimeout);
    const domCache = populateDomCache(currentInstance);
    render(currentInstance, innerParams);
    privateProps.innerParams.set(currentInstance, innerParams);
    return swalPromise(currentInstance, domCache, innerParams);
  }
  // `catch` cannot be the name of a module export, so we define our thenable methods here instead
  then(onFulfilled) {
    return _classPrivateFieldGet2(_promise, this).then(onFulfilled);
  }
  finally(onFinally) {
    return _classPrivateFieldGet2(_promise, this).finally(onFinally);
  }
};
var swalPromise = (instance, domCache, innerParams) => {
  return new Promise((resolve, reject) => {
    const dismissWith = (dismiss) => {
      instance.close({
        isDismissed: true,
        dismiss
      });
    };
    privateMethods.swalPromiseResolve.set(instance, resolve);
    privateMethods.swalPromiseReject.set(instance, reject);
    domCache.confirmButton.onclick = () => {
      handleConfirmButtonClick(instance);
    };
    domCache.denyButton.onclick = () => {
      handleDenyButtonClick(instance);
    };
    domCache.cancelButton.onclick = () => {
      handleCancelButtonClick(instance, dismissWith);
    };
    domCache.closeButton.onclick = () => {
      dismissWith(DismissReason.close);
    };
    handlePopupClick(innerParams, domCache, dismissWith);
    addKeydownHandler(globalState, innerParams, dismissWith);
    handleInputOptionsAndValue(instance, innerParams);
    openPopup(innerParams);
    setupTimer(globalState, innerParams, dismissWith);
    initFocus(domCache, innerParams);
    setTimeout(() => {
      domCache.container.scrollTop = 0;
    });
  });
};
var prepareParams = (userParams, mixinParams) => {
  const templateParams = getTemplateParams(userParams);
  const params = Object.assign({}, defaultParams, mixinParams, templateParams, userParams);
  params.showClass = Object.assign({}, defaultParams.showClass, params.showClass);
  params.hideClass = Object.assign({}, defaultParams.hideClass, params.hideClass);
  if (params.animation === false) {
    params.showClass = {
      backdrop: "swal2-noanimation"
    };
    params.hideClass = {};
  }
  return params;
};
var populateDomCache = (instance) => {
  const domCache = {
    popup: getPopup(),
    container: getContainer(),
    actions: getActions(),
    confirmButton: getConfirmButton(),
    denyButton: getDenyButton(),
    cancelButton: getCancelButton(),
    loader: getLoader(),
    closeButton: getCloseButton(),
    validationMessage: getValidationMessage(),
    progressSteps: getProgressSteps()
  };
  privateProps.domCache.set(instance, domCache);
  return domCache;
};
var setupTimer = (globalState2, innerParams, dismissWith) => {
  const timerProgressBar = getTimerProgressBar();
  hide(timerProgressBar);
  if (innerParams.timer) {
    globalState2.timeout = new Timer(() => {
      dismissWith("timer");
      delete globalState2.timeout;
    }, innerParams.timer);
    if (innerParams.timerProgressBar) {
      show(timerProgressBar);
      applyCustomClass(timerProgressBar, innerParams, "timerProgressBar");
      setTimeout(() => {
        if (globalState2.timeout && globalState2.timeout.running) {
          animateTimerProgressBar(innerParams.timer);
        }
      });
    }
  }
};
var initFocus = (domCache, innerParams) => {
  if (innerParams.toast) {
    return;
  }
  if (!callIfFunction(innerParams.allowEnterKey)) {
    warnAboutDeprecation("allowEnterKey");
    blurActiveElement();
    return;
  }
  if (focusAutofocus(domCache)) {
    return;
  }
  if (focusButton(domCache, innerParams)) {
    return;
  }
  setFocus(-1, 1);
};
var focusAutofocus = (domCache) => {
  const autofocusElements = Array.from(domCache.popup.querySelectorAll("[autofocus]"));
  for (const autofocusElement of autofocusElements) {
    if (autofocusElement instanceof HTMLElement && isVisible$1(autofocusElement)) {
      autofocusElement.focus();
      return true;
    }
  }
  return false;
};
var focusButton = (domCache, innerParams) => {
  if (innerParams.focusDeny && isVisible$1(domCache.denyButton)) {
    domCache.denyButton.focus();
    return true;
  }
  if (innerParams.focusCancel && isVisible$1(domCache.cancelButton)) {
    domCache.cancelButton.focus();
    return true;
  }
  if (innerParams.focusConfirm && isVisible$1(domCache.confirmButton)) {
    domCache.confirmButton.focus();
    return true;
  }
  return false;
};
var blurActiveElement = () => {
  if (document.activeElement instanceof HTMLElement && typeof document.activeElement.blur === "function") {
    document.activeElement.blur();
  }
};
SweetAlert.prototype.disableButtons = disableButtons;
SweetAlert.prototype.enableButtons = enableButtons;
SweetAlert.prototype.getInput = getInput;
SweetAlert.prototype.disableInput = disableInput;
SweetAlert.prototype.enableInput = enableInput;
SweetAlert.prototype.hideLoading = hideLoading;
SweetAlert.prototype.disableLoading = hideLoading;
SweetAlert.prototype.showValidationMessage = showValidationMessage;
SweetAlert.prototype.resetValidationMessage = resetValidationMessage;
SweetAlert.prototype.close = close;
SweetAlert.prototype.closePopup = close;
SweetAlert.prototype.closeModal = close;
SweetAlert.prototype.closeToast = close;
SweetAlert.prototype.rejectPromise = rejectPromise;
SweetAlert.prototype.update = update;
SweetAlert.prototype._destroy = _destroy;
Object.assign(SweetAlert, staticMethods);
Object.keys(instanceMethods).forEach((key) => {
  SweetAlert[key] = function(...args) {
    if (currentInstance && currentInstance[key]) {
      return currentInstance[key](...args);
    }
    return null;
  };
});
SweetAlert.DismissReason = DismissReason;
SweetAlert.version = "11.22.5";
var Swal = SweetAlert;
Swal.default = Swal;
"undefined" != typeof document && function(e, t) {
  var n = e.createElement("style");
  if (e.getElementsByTagName("head")[0].appendChild(n), n.styleSheet) n.styleSheet.disabled || (n.styleSheet.cssText = t);
  else try {
    n.innerHTML = t;
  } catch (e2) {
    n.innerText = t;
  }
}(document, ':root{--swal2-outline: 0 0 0 3px rgba(100, 150, 200, 0.5);--swal2-container-padding: 0.625em;--swal2-backdrop: rgba(0, 0, 0, 0.4);--swal2-backdrop-transition: background-color 0.1s;--swal2-width: 32em;--swal2-padding: 0 0 1.25em;--swal2-border: none;--swal2-border-radius: 0.3125rem;--swal2-background: white;--swal2-color: #545454;--swal2-show-animation: swal2-show 0.3s;--swal2-hide-animation: swal2-hide 0.15s forwards;--swal2-icon-zoom: 1;--swal2-icon-animations: true;--swal2-title-padding: 0.8em 1em 0;--swal2-html-container-padding: 1em 1.6em 0.3em;--swal2-input-border: 1px solid #d9d9d9;--swal2-input-border-radius: 0.1875em;--swal2-input-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.06), 0 0 0 3px transparent;--swal2-input-background: transparent;--swal2-input-transition: border-color 0.2s, box-shadow 0.2s;--swal2-input-hover-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.06), 0 0 0 3px transparent;--swal2-input-focus-border: 1px solid #b4dbed;--swal2-input-focus-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.06), 0 0 0 3px $swal2-outline-color;--swal2-progress-step-background: #add8e6;--swal2-validation-message-background: #f0f0f0;--swal2-validation-message-color: #666;--swal2-footer-border-color: #eee;--swal2-footer-background: transparent;--swal2-footer-color: inherit;--swal2-timer-progress-bar-background: rgba(0, 0, 0, 0.3);--swal2-close-button-position: initial;--swal2-close-button-inset: auto;--swal2-close-button-font-size: 2.5em;--swal2-close-button-color: #ccc;--swal2-close-button-transition: color 0.2s, box-shadow 0.2s;--swal2-close-button-outline: initial;--swal2-close-button-box-shadow: inset 0 0 0 3px transparent;--swal2-close-button-focus-box-shadow: inset var(--swal2-outline);--swal2-close-button-hover-transform: none;--swal2-actions-justify-content: center;--swal2-actions-width: auto;--swal2-actions-margin: 1.25em auto 0;--swal2-actions-padding: 0;--swal2-actions-border-radius: 0;--swal2-actions-background: transparent;--swal2-action-button-transition: background-color 0.2s, box-shadow 0.2s;--swal2-action-button-hover: black 10%;--swal2-action-button-active: black 10%;--swal2-confirm-button-box-shadow: none;--swal2-confirm-button-border-radius: 0.25em;--swal2-confirm-button-background-color: #7066e0;--swal2-confirm-button-color: #fff;--swal2-deny-button-box-shadow: none;--swal2-deny-button-border-radius: 0.25em;--swal2-deny-button-background-color: #dc3741;--swal2-deny-button-color: #fff;--swal2-cancel-button-box-shadow: none;--swal2-cancel-button-border-radius: 0.25em;--swal2-cancel-button-background-color: #6e7881;--swal2-cancel-button-color: #fff;--swal2-toast-show-animation: swal2-toast-show 0.5s;--swal2-toast-hide-animation: swal2-toast-hide 0.1s forwards;--swal2-toast-border: none;--swal2-toast-box-shadow: 0 0 1px hsl(0deg 0% 0% / 0.075), 0 1px 2px hsl(0deg 0% 0% / 0.075), 1px 2px 4px hsl(0deg 0% 0% / 0.075), 1px 3px 8px hsl(0deg 0% 0% / 0.075), 2px 4px 16px hsl(0deg 0% 0% / 0.075)}[data-swal2-theme=dark]{--swal2-dark-theme-black: #19191a;--swal2-dark-theme-white: #e1e1e1;--swal2-background: var(--swal2-dark-theme-black);--swal2-color: var(--swal2-dark-theme-white);--swal2-footer-border-color: #555;--swal2-input-background: color-mix(in srgb, var(--swal2-dark-theme-black), var(--swal2-dark-theme-white) 10%);--swal2-validation-message-background: color-mix( in srgb, var(--swal2-dark-theme-black), var(--swal2-dark-theme-white) 10% );--swal2-validation-message-color: var(--swal2-dark-theme-white);--swal2-timer-progress-bar-background: rgba(255, 255, 255, 0.7)}@media(prefers-color-scheme: dark){[data-swal2-theme=auto]{--swal2-dark-theme-black: #19191a;--swal2-dark-theme-white: #e1e1e1;--swal2-background: var(--swal2-dark-theme-black);--swal2-color: var(--swal2-dark-theme-white);--swal2-footer-border-color: #555;--swal2-input-background: color-mix(in srgb, var(--swal2-dark-theme-black), var(--swal2-dark-theme-white) 10%);--swal2-validation-message-background: color-mix( in srgb, var(--swal2-dark-theme-black), var(--swal2-dark-theme-white) 10% );--swal2-validation-message-color: var(--swal2-dark-theme-white);--swal2-timer-progress-bar-background: rgba(255, 255, 255, 0.7)}}body.swal2-shown:not(.swal2-no-backdrop,.swal2-toast-shown){overflow:hidden}body.swal2-height-auto{height:auto !important}body.swal2-no-backdrop .swal2-container{background-color:rgba(0,0,0,0) !important;pointer-events:none}body.swal2-no-backdrop .swal2-container .swal2-popup{pointer-events:all}body.swal2-no-backdrop .swal2-container .swal2-modal{box-shadow:0 0 10px var(--swal2-backdrop)}body.swal2-toast-shown .swal2-container{box-sizing:border-box;width:360px;max-width:100%;background-color:rgba(0,0,0,0);pointer-events:none}body.swal2-toast-shown .swal2-container.swal2-top{inset:0 auto auto 50%;transform:translateX(-50%)}body.swal2-toast-shown .swal2-container.swal2-top-end,body.swal2-toast-shown .swal2-container.swal2-top-right{inset:0 0 auto auto}body.swal2-toast-shown .swal2-container.swal2-top-start,body.swal2-toast-shown .swal2-container.swal2-top-left{inset:0 auto auto 0}body.swal2-toast-shown .swal2-container.swal2-center-start,body.swal2-toast-shown .swal2-container.swal2-center-left{inset:50% auto auto 0;transform:translateY(-50%)}body.swal2-toast-shown .swal2-container.swal2-center{inset:50% auto auto 50%;transform:translate(-50%, -50%)}body.swal2-toast-shown .swal2-container.swal2-center-end,body.swal2-toast-shown .swal2-container.swal2-center-right{inset:50% 0 auto auto;transform:translateY(-50%)}body.swal2-toast-shown .swal2-container.swal2-bottom-start,body.swal2-toast-shown .swal2-container.swal2-bottom-left{inset:auto auto 0 0}body.swal2-toast-shown .swal2-container.swal2-bottom{inset:auto auto 0 50%;transform:translateX(-50%)}body.swal2-toast-shown .swal2-container.swal2-bottom-end,body.swal2-toast-shown .swal2-container.swal2-bottom-right{inset:auto 0 0 auto}@media print{body.swal2-shown:not(.swal2-no-backdrop,.swal2-toast-shown){overflow-y:scroll !important}body.swal2-shown:not(.swal2-no-backdrop,.swal2-toast-shown)>[aria-hidden=true]{display:none}body.swal2-shown:not(.swal2-no-backdrop,.swal2-toast-shown) .swal2-container{position:static !important}}div:where(.swal2-container){display:grid;position:fixed;z-index:1060;inset:0;box-sizing:border-box;grid-template-areas:"top-start     top            top-end" "center-start  center         center-end" "bottom-start  bottom-center  bottom-end";grid-template-rows:minmax(min-content, auto) minmax(min-content, auto) minmax(min-content, auto);height:100%;padding:var(--swal2-container-padding);overflow-x:hidden;transition:var(--swal2-backdrop-transition);-webkit-overflow-scrolling:touch}div:where(.swal2-container).swal2-backdrop-show,div:where(.swal2-container).swal2-noanimation{background:var(--swal2-backdrop)}div:where(.swal2-container).swal2-backdrop-hide{background:rgba(0,0,0,0) !important}div:where(.swal2-container).swal2-top-start,div:where(.swal2-container).swal2-center-start,div:where(.swal2-container).swal2-bottom-start{grid-template-columns:minmax(0, 1fr) auto auto}div:where(.swal2-container).swal2-top,div:where(.swal2-container).swal2-center,div:where(.swal2-container).swal2-bottom{grid-template-columns:auto minmax(0, 1fr) auto}div:where(.swal2-container).swal2-top-end,div:where(.swal2-container).swal2-center-end,div:where(.swal2-container).swal2-bottom-end{grid-template-columns:auto auto minmax(0, 1fr)}div:where(.swal2-container).swal2-top-start>.swal2-popup{align-self:start}div:where(.swal2-container).swal2-top>.swal2-popup{grid-column:2;place-self:start center}div:where(.swal2-container).swal2-top-end>.swal2-popup,div:where(.swal2-container).swal2-top-right>.swal2-popup{grid-column:3;place-self:start end}div:where(.swal2-container).swal2-center-start>.swal2-popup,div:where(.swal2-container).swal2-center-left>.swal2-popup{grid-row:2;align-self:center}div:where(.swal2-container).swal2-center>.swal2-popup{grid-column:2;grid-row:2;place-self:center center}div:where(.swal2-container).swal2-center-end>.swal2-popup,div:where(.swal2-container).swal2-center-right>.swal2-popup{grid-column:3;grid-row:2;place-self:center end}div:where(.swal2-container).swal2-bottom-start>.swal2-popup,div:where(.swal2-container).swal2-bottom-left>.swal2-popup{grid-column:1;grid-row:3;align-self:end}div:where(.swal2-container).swal2-bottom>.swal2-popup{grid-column:2;grid-row:3;place-self:end center}div:where(.swal2-container).swal2-bottom-end>.swal2-popup,div:where(.swal2-container).swal2-bottom-right>.swal2-popup{grid-column:3;grid-row:3;place-self:end end}div:where(.swal2-container).swal2-grow-row>.swal2-popup,div:where(.swal2-container).swal2-grow-fullscreen>.swal2-popup{grid-column:1/4;width:100%}div:where(.swal2-container).swal2-grow-column>.swal2-popup,div:where(.swal2-container).swal2-grow-fullscreen>.swal2-popup{grid-row:1/4;align-self:stretch}div:where(.swal2-container).swal2-no-transition{transition:none !important}div:where(.swal2-container)[popover]{width:auto;border:0}div:where(.swal2-container) div:where(.swal2-popup){display:none;position:relative;box-sizing:border-box;grid-template-columns:minmax(0, 100%);width:var(--swal2-width);max-width:100%;padding:var(--swal2-padding);border:var(--swal2-border);border-radius:var(--swal2-border-radius);background:var(--swal2-background);color:var(--swal2-color);font-family:inherit;font-size:1rem;container-name:swal2-popup}div:where(.swal2-container) div:where(.swal2-popup):focus{outline:none}div:where(.swal2-container) div:where(.swal2-popup).swal2-loading{overflow-y:hidden}div:where(.swal2-container) div:where(.swal2-popup).swal2-draggable{cursor:grab}div:where(.swal2-container) div:where(.swal2-popup).swal2-draggable div:where(.swal2-icon){cursor:grab}div:where(.swal2-container) div:where(.swal2-popup).swal2-dragging{cursor:grabbing}div:where(.swal2-container) div:where(.swal2-popup).swal2-dragging div:where(.swal2-icon){cursor:grabbing}div:where(.swal2-container) h2:where(.swal2-title){position:relative;max-width:100%;margin:0;padding:var(--swal2-title-padding);color:inherit;font-size:1.875em;font-weight:600;text-align:center;text-transform:none;word-wrap:break-word;cursor:initial}div:where(.swal2-container) div:where(.swal2-actions){display:flex;z-index:1;box-sizing:border-box;flex-wrap:wrap;align-items:center;justify-content:var(--swal2-actions-justify-content);width:var(--swal2-actions-width);margin:var(--swal2-actions-margin);padding:var(--swal2-actions-padding);border-radius:var(--swal2-actions-border-radius);background:var(--swal2-actions-background)}div:where(.swal2-container) div:where(.swal2-loader){display:none;align-items:center;justify-content:center;width:2.2em;height:2.2em;margin:0 1.875em;animation:swal2-rotate-loading 1.5s linear 0s infinite normal;border-width:.25em;border-style:solid;border-radius:100%;border-color:#2778c4 rgba(0,0,0,0) #2778c4 rgba(0,0,0,0)}div:where(.swal2-container) button:where(.swal2-styled){margin:.3125em;padding:.625em 1.1em;transition:var(--swal2-action-button-transition);border:none;box-shadow:0 0 0 3px rgba(0,0,0,0);font-weight:500}div:where(.swal2-container) button:where(.swal2-styled):not([disabled]){cursor:pointer}div:where(.swal2-container) button:where(.swal2-styled):where(.swal2-confirm){border-radius:var(--swal2-confirm-button-border-radius);background:initial;background-color:var(--swal2-confirm-button-background-color);box-shadow:var(--swal2-confirm-button-box-shadow);color:var(--swal2-confirm-button-color);font-size:1em}div:where(.swal2-container) button:where(.swal2-styled):where(.swal2-confirm):hover{background-color:color-mix(in srgb, var(--swal2-confirm-button-background-color), var(--swal2-action-button-hover))}div:where(.swal2-container) button:where(.swal2-styled):where(.swal2-confirm):active{background-color:color-mix(in srgb, var(--swal2-confirm-button-background-color), var(--swal2-action-button-active))}div:where(.swal2-container) button:where(.swal2-styled):where(.swal2-deny){border-radius:var(--swal2-deny-button-border-radius);background:initial;background-color:var(--swal2-deny-button-background-color);box-shadow:var(--swal2-deny-button-box-shadow);color:var(--swal2-deny-button-color);font-size:1em}div:where(.swal2-container) button:where(.swal2-styled):where(.swal2-deny):hover{background-color:color-mix(in srgb, var(--swal2-deny-button-background-color), var(--swal2-action-button-hover))}div:where(.swal2-container) button:where(.swal2-styled):where(.swal2-deny):active{background-color:color-mix(in srgb, var(--swal2-deny-button-background-color), var(--swal2-action-button-active))}div:where(.swal2-container) button:where(.swal2-styled):where(.swal2-cancel){border-radius:var(--swal2-cancel-button-border-radius);background:initial;background-color:var(--swal2-cancel-button-background-color);box-shadow:var(--swal2-cancel-button-box-shadow);color:var(--swal2-cancel-button-color);font-size:1em}div:where(.swal2-container) button:where(.swal2-styled):where(.swal2-cancel):hover{background-color:color-mix(in srgb, var(--swal2-cancel-button-background-color), var(--swal2-action-button-hover))}div:where(.swal2-container) button:where(.swal2-styled):where(.swal2-cancel):active{background-color:color-mix(in srgb, var(--swal2-cancel-button-background-color), var(--swal2-action-button-active))}div:where(.swal2-container) button:where(.swal2-styled):focus-visible{outline:none;box-shadow:var(--swal2-action-button-focus-box-shadow)}div:where(.swal2-container) button:where(.swal2-styled)[disabled]:not(.swal2-loading){opacity:.4}div:where(.swal2-container) button:where(.swal2-styled)::-moz-focus-inner{border:0}div:where(.swal2-container) div:where(.swal2-footer){margin:1em 0 0;padding:1em 1em 0;border-top:1px solid var(--swal2-footer-border-color);background:var(--swal2-footer-background);color:var(--swal2-footer-color);font-size:1em;text-align:center;cursor:initial}div:where(.swal2-container) .swal2-timer-progress-bar-container{position:absolute;right:0;bottom:0;left:0;grid-column:auto !important;overflow:hidden;border-bottom-right-radius:var(--swal2-border-radius);border-bottom-left-radius:var(--swal2-border-radius)}div:where(.swal2-container) div:where(.swal2-timer-progress-bar){width:100%;height:.25em;background:var(--swal2-timer-progress-bar-background)}div:where(.swal2-container) img:where(.swal2-image){max-width:100%;margin:2em auto 1em;cursor:initial}div:where(.swal2-container) button:where(.swal2-close){position:var(--swal2-close-button-position);inset:var(--swal2-close-button-inset);z-index:2;align-items:center;justify-content:center;width:1.2em;height:1.2em;margin-top:0;margin-right:0;margin-bottom:-1.2em;padding:0;overflow:hidden;transition:var(--swal2-close-button-transition);border:none;border-radius:var(--swal2-border-radius);outline:var(--swal2-close-button-outline);background:rgba(0,0,0,0);color:var(--swal2-close-button-color);font-family:monospace;font-size:var(--swal2-close-button-font-size);cursor:pointer;justify-self:end}div:where(.swal2-container) button:where(.swal2-close):hover{transform:var(--swal2-close-button-hover-transform);background:rgba(0,0,0,0);color:#f27474}div:where(.swal2-container) button:where(.swal2-close):focus-visible{outline:none;box-shadow:var(--swal2-close-button-focus-box-shadow)}div:where(.swal2-container) button:where(.swal2-close)::-moz-focus-inner{border:0}div:where(.swal2-container) div:where(.swal2-html-container){z-index:1;justify-content:center;margin:0;padding:var(--swal2-html-container-padding);overflow:auto;color:inherit;font-size:1.125em;font-weight:normal;line-height:normal;text-align:center;word-wrap:break-word;word-break:break-word;cursor:initial}div:where(.swal2-container) input:where(.swal2-input),div:where(.swal2-container) input:where(.swal2-file),div:where(.swal2-container) textarea:where(.swal2-textarea),div:where(.swal2-container) select:where(.swal2-select),div:where(.swal2-container) div:where(.swal2-radio),div:where(.swal2-container) label:where(.swal2-checkbox){margin:1em 2em 3px}div:where(.swal2-container) input:where(.swal2-input),div:where(.swal2-container) input:where(.swal2-file),div:where(.swal2-container) textarea:where(.swal2-textarea){box-sizing:border-box;width:auto;transition:var(--swal2-input-transition);border:var(--swal2-input-border);border-radius:var(--swal2-input-border-radius);background:var(--swal2-input-background);box-shadow:var(--swal2-input-box-shadow);color:inherit;font-size:1.125em}div:where(.swal2-container) input:where(.swal2-input).swal2-inputerror,div:where(.swal2-container) input:where(.swal2-file).swal2-inputerror,div:where(.swal2-container) textarea:where(.swal2-textarea).swal2-inputerror{border-color:#f27474 !important;box-shadow:0 0 2px #f27474 !important}div:where(.swal2-container) input:where(.swal2-input):hover,div:where(.swal2-container) input:where(.swal2-file):hover,div:where(.swal2-container) textarea:where(.swal2-textarea):hover{box-shadow:var(--swal2-input-hover-box-shadow)}div:where(.swal2-container) input:where(.swal2-input):focus,div:where(.swal2-container) input:where(.swal2-file):focus,div:where(.swal2-container) textarea:where(.swal2-textarea):focus{border:var(--swal2-input-focus-border);outline:none;box-shadow:var(--swal2-input-focus-box-shadow)}div:where(.swal2-container) input:where(.swal2-input)::placeholder,div:where(.swal2-container) input:where(.swal2-file)::placeholder,div:where(.swal2-container) textarea:where(.swal2-textarea)::placeholder{color:#ccc}div:where(.swal2-container) .swal2-range{margin:1em 2em 3px;background:var(--swal2-background)}div:where(.swal2-container) .swal2-range input{width:80%}div:where(.swal2-container) .swal2-range output{width:20%;color:inherit;font-weight:600;text-align:center}div:where(.swal2-container) .swal2-range input,div:where(.swal2-container) .swal2-range output{height:2.625em;padding:0;font-size:1.125em;line-height:2.625em}div:where(.swal2-container) .swal2-input{height:2.625em;padding:0 .75em}div:where(.swal2-container) .swal2-file{width:75%;margin-right:auto;margin-left:auto;background:var(--swal2-input-background);font-size:1.125em}div:where(.swal2-container) .swal2-textarea{height:6.75em;padding:.75em}div:where(.swal2-container) .swal2-select{min-width:50%;max-width:100%;padding:.375em .625em;background:var(--swal2-input-background);color:inherit;font-size:1.125em}div:where(.swal2-container) .swal2-radio,div:where(.swal2-container) .swal2-checkbox{align-items:center;justify-content:center;background:var(--swal2-background);color:inherit}div:where(.swal2-container) .swal2-radio label,div:where(.swal2-container) .swal2-checkbox label{margin:0 .6em;font-size:1.125em}div:where(.swal2-container) .swal2-radio input,div:where(.swal2-container) .swal2-checkbox input{flex-shrink:0;margin:0 .4em}div:where(.swal2-container) label:where(.swal2-input-label){display:flex;justify-content:center;margin:1em auto 0}div:where(.swal2-container) div:where(.swal2-validation-message){align-items:center;justify-content:center;margin:1em 0 0;padding:.625em;overflow:hidden;background:var(--swal2-validation-message-background);color:var(--swal2-validation-message-color);font-size:1em;font-weight:300}div:where(.swal2-container) div:where(.swal2-validation-message)::before{content:"!";display:inline-block;width:1.5em;min-width:1.5em;height:1.5em;margin:0 .625em;border-radius:50%;background-color:#f27474;color:#fff;font-weight:600;line-height:1.5em;text-align:center}div:where(.swal2-container) .swal2-progress-steps{flex-wrap:wrap;align-items:center;max-width:100%;margin:1.25em auto;padding:0;background:rgba(0,0,0,0);font-weight:600}div:where(.swal2-container) .swal2-progress-steps li{display:inline-block;position:relative}div:where(.swal2-container) .swal2-progress-steps .swal2-progress-step{z-index:20;flex-shrink:0;width:2em;height:2em;border-radius:2em;background:#2778c4;color:#fff;line-height:2em;text-align:center}div:where(.swal2-container) .swal2-progress-steps .swal2-progress-step.swal2-active-progress-step{background:#2778c4}div:where(.swal2-container) .swal2-progress-steps .swal2-progress-step.swal2-active-progress-step~.swal2-progress-step{background:var(--swal2-progress-step-background);color:#fff}div:where(.swal2-container) .swal2-progress-steps .swal2-progress-step.swal2-active-progress-step~.swal2-progress-step-line{background:var(--swal2-progress-step-background)}div:where(.swal2-container) .swal2-progress-steps .swal2-progress-step-line{z-index:10;flex-shrink:0;width:2.5em;height:.4em;margin:0 -1px;background:#2778c4}div:where(.swal2-icon){position:relative;box-sizing:content-box;justify-content:center;width:5em;height:5em;margin:2.5em auto .6em;zoom:var(--swal2-icon-zoom);border:.25em solid rgba(0,0,0,0);border-radius:50%;border-color:#000;font-family:inherit;line-height:5em;cursor:default;user-select:none}div:where(.swal2-icon) .swal2-icon-content{display:flex;align-items:center;font-size:3.75em}div:where(.swal2-icon).swal2-error{border-color:#f27474;color:#f27474}div:where(.swal2-icon).swal2-error .swal2-x-mark{position:relative;flex-grow:1}div:where(.swal2-icon).swal2-error [class^=swal2-x-mark-line]{display:block;position:absolute;top:2.3125em;width:2.9375em;height:.3125em;border-radius:.125em;background-color:#f27474}div:where(.swal2-icon).swal2-error [class^=swal2-x-mark-line][class$=left]{left:1.0625em;transform:rotate(45deg)}div:where(.swal2-icon).swal2-error [class^=swal2-x-mark-line][class$=right]{right:1em;transform:rotate(-45deg)}@container swal2-popup style(--swal2-icon-animations:true){div:where(.swal2-icon).swal2-error.swal2-icon-show{animation:swal2-animate-error-icon .5s}div:where(.swal2-icon).swal2-error.swal2-icon-show .swal2-x-mark{animation:swal2-animate-error-x-mark .5s}}div:where(.swal2-icon).swal2-warning{border-color:#f8bb86;color:#f8bb86}@container swal2-popup style(--swal2-icon-animations:true){div:where(.swal2-icon).swal2-warning.swal2-icon-show{animation:swal2-animate-error-icon .5s}div:where(.swal2-icon).swal2-warning.swal2-icon-show .swal2-icon-content{animation:swal2-animate-i-mark .5s}}div:where(.swal2-icon).swal2-info{border-color:#3fc3ee;color:#3fc3ee}@container swal2-popup style(--swal2-icon-animations:true){div:where(.swal2-icon).swal2-info.swal2-icon-show{animation:swal2-animate-error-icon .5s}div:where(.swal2-icon).swal2-info.swal2-icon-show .swal2-icon-content{animation:swal2-animate-i-mark .8s}}div:where(.swal2-icon).swal2-question{border-color:#87adbd;color:#87adbd}@container swal2-popup style(--swal2-icon-animations:true){div:where(.swal2-icon).swal2-question.swal2-icon-show{animation:swal2-animate-error-icon .5s}div:where(.swal2-icon).swal2-question.swal2-icon-show .swal2-icon-content{animation:swal2-animate-question-mark .8s}}div:where(.swal2-icon).swal2-success{border-color:#a5dc86;color:#a5dc86}div:where(.swal2-icon).swal2-success [class^=swal2-success-circular-line]{position:absolute;width:3.75em;height:7.5em;border-radius:50%}div:where(.swal2-icon).swal2-success [class^=swal2-success-circular-line][class$=left]{top:-0.4375em;left:-2.0635em;transform:rotate(-45deg);transform-origin:3.75em 3.75em;border-radius:7.5em 0 0 7.5em}div:where(.swal2-icon).swal2-success [class^=swal2-success-circular-line][class$=right]{top:-0.6875em;left:1.875em;transform:rotate(-45deg);transform-origin:0 3.75em;border-radius:0 7.5em 7.5em 0}div:where(.swal2-icon).swal2-success .swal2-success-ring{position:absolute;z-index:2;top:-0.25em;left:-0.25em;box-sizing:content-box;width:100%;height:100%;border:.25em solid rgba(165,220,134,.3);border-radius:50%}div:where(.swal2-icon).swal2-success .swal2-success-fix{position:absolute;z-index:1;top:.5em;left:1.625em;width:.4375em;height:5.625em;transform:rotate(-45deg)}div:where(.swal2-icon).swal2-success [class^=swal2-success-line]{display:block;position:absolute;z-index:2;height:.3125em;border-radius:.125em;background-color:#a5dc86}div:where(.swal2-icon).swal2-success [class^=swal2-success-line][class$=tip]{top:2.875em;left:.8125em;width:1.5625em;transform:rotate(45deg)}div:where(.swal2-icon).swal2-success [class^=swal2-success-line][class$=long]{top:2.375em;right:.5em;width:2.9375em;transform:rotate(-45deg)}@container swal2-popup style(--swal2-icon-animations:true){div:where(.swal2-icon).swal2-success.swal2-icon-show .swal2-success-line-tip{animation:swal2-animate-success-line-tip .75s}div:where(.swal2-icon).swal2-success.swal2-icon-show .swal2-success-line-long{animation:swal2-animate-success-line-long .75s}div:where(.swal2-icon).swal2-success.swal2-icon-show .swal2-success-circular-line-right{animation:swal2-rotate-success-circular-line 4.25s ease-in}}[class^=swal2]{-webkit-tap-highlight-color:rgba(0,0,0,0)}.swal2-show{animation:var(--swal2-show-animation)}.swal2-hide{animation:var(--swal2-hide-animation)}.swal2-noanimation{transition:none}.swal2-scrollbar-measure{position:absolute;top:-9999px;width:50px;height:50px;overflow:scroll}.swal2-rtl .swal2-close{margin-right:initial;margin-left:0}.swal2-rtl .swal2-timer-progress-bar{right:0;left:auto}.swal2-toast{box-sizing:border-box;grid-column:1/4 !important;grid-row:1/4 !important;grid-template-columns:min-content auto min-content;padding:1em;overflow-y:hidden;border:var(--swal2-toast-border);background:var(--swal2-background);box-shadow:var(--swal2-toast-box-shadow);pointer-events:all}.swal2-toast>*{grid-column:2}.swal2-toast h2:where(.swal2-title){margin:.5em 1em;padding:0;font-size:1em;text-align:initial}.swal2-toast .swal2-loading{justify-content:center}.swal2-toast input:where(.swal2-input){height:2em;margin:.5em;font-size:1em}.swal2-toast .swal2-validation-message{font-size:1em}.swal2-toast div:where(.swal2-footer){margin:.5em 0 0;padding:.5em 0 0;font-size:.8em}.swal2-toast button:where(.swal2-close){grid-column:3/3;grid-row:1/99;align-self:center;width:.8em;height:.8em;margin:0;font-size:2em}.swal2-toast div:where(.swal2-html-container){margin:.5em 1em;padding:0;overflow:initial;font-size:1em;text-align:initial}.swal2-toast div:where(.swal2-html-container):empty{padding:0}.swal2-toast .swal2-loader{grid-column:1;grid-row:1/99;align-self:center;width:2em;height:2em;margin:.25em}.swal2-toast .swal2-icon{grid-column:1;grid-row:1/99;align-self:center;width:2em;min-width:2em;height:2em;margin:0 .5em 0 0}.swal2-toast .swal2-icon .swal2-icon-content{display:flex;align-items:center;font-size:1.8em;font-weight:bold}.swal2-toast .swal2-icon.swal2-success .swal2-success-ring{width:2em;height:2em}.swal2-toast .swal2-icon.swal2-error [class^=swal2-x-mark-line]{top:.875em;width:1.375em}.swal2-toast .swal2-icon.swal2-error [class^=swal2-x-mark-line][class$=left]{left:.3125em}.swal2-toast .swal2-icon.swal2-error [class^=swal2-x-mark-line][class$=right]{right:.3125em}.swal2-toast div:where(.swal2-actions){justify-content:flex-start;height:auto;margin:0;margin-top:.5em;padding:0 .5em}.swal2-toast button:where(.swal2-styled){margin:.25em .5em;padding:.4em .6em;font-size:1em}.swal2-toast .swal2-success{border-color:#a5dc86}.swal2-toast .swal2-success [class^=swal2-success-circular-line]{position:absolute;width:1.6em;height:3em;border-radius:50%}.swal2-toast .swal2-success [class^=swal2-success-circular-line][class$=left]{top:-0.8em;left:-0.5em;transform:rotate(-45deg);transform-origin:2em 2em;border-radius:4em 0 0 4em}.swal2-toast .swal2-success [class^=swal2-success-circular-line][class$=right]{top:-0.25em;left:.9375em;transform-origin:0 1.5em;border-radius:0 4em 4em 0}.swal2-toast .swal2-success .swal2-success-ring{width:2em;height:2em}.swal2-toast .swal2-success .swal2-success-fix{top:0;left:.4375em;width:.4375em;height:2.6875em}.swal2-toast .swal2-success [class^=swal2-success-line]{height:.3125em}.swal2-toast .swal2-success [class^=swal2-success-line][class$=tip]{top:1.125em;left:.1875em;width:.75em}.swal2-toast .swal2-success [class^=swal2-success-line][class$=long]{top:.9375em;right:.1875em;width:1.375em}@container swal2-popup style(--swal2-icon-animations:true){.swal2-toast .swal2-success.swal2-icon-show .swal2-success-line-tip{animation:swal2-toast-animate-success-line-tip .75s}.swal2-toast .swal2-success.swal2-icon-show .swal2-success-line-long{animation:swal2-toast-animate-success-line-long .75s}}.swal2-toast.swal2-show{animation:var(--swal2-toast-show-animation)}.swal2-toast.swal2-hide{animation:var(--swal2-toast-hide-animation)}@keyframes swal2-show{0%{transform:scale(0.7)}45%{transform:scale(1.05)}80%{transform:scale(0.95)}100%{transform:scale(1)}}@keyframes swal2-hide{0%{transform:scale(1);opacity:1}100%{transform:scale(0.5);opacity:0}}@keyframes swal2-animate-success-line-tip{0%{top:1.1875em;left:.0625em;width:0}54%{top:1.0625em;left:.125em;width:0}70%{top:2.1875em;left:-0.375em;width:3.125em}84%{top:3em;left:1.3125em;width:1.0625em}100%{top:2.8125em;left:.8125em;width:1.5625em}}@keyframes swal2-animate-success-line-long{0%{top:3.375em;right:2.875em;width:0}65%{top:3.375em;right:2.875em;width:0}84%{top:2.1875em;right:0;width:3.4375em}100%{top:2.375em;right:.5em;width:2.9375em}}@keyframes swal2-rotate-success-circular-line{0%{transform:rotate(-45deg)}5%{transform:rotate(-45deg)}12%{transform:rotate(-405deg)}100%{transform:rotate(-405deg)}}@keyframes swal2-animate-error-x-mark{0%{margin-top:1.625em;transform:scale(0.4);opacity:0}50%{margin-top:1.625em;transform:scale(0.4);opacity:0}80%{margin-top:-0.375em;transform:scale(1.15)}100%{margin-top:0;transform:scale(1);opacity:1}}@keyframes swal2-animate-error-icon{0%{transform:rotateX(100deg);opacity:0}100%{transform:rotateX(0deg);opacity:1}}@keyframes swal2-rotate-loading{0%{transform:rotate(0deg)}100%{transform:rotate(360deg)}}@keyframes swal2-animate-question-mark{0%{transform:rotateY(-360deg)}100%{transform:rotateY(0)}}@keyframes swal2-animate-i-mark{0%{transform:rotateZ(45deg);opacity:0}25%{transform:rotateZ(-25deg);opacity:.4}50%{transform:rotateZ(15deg);opacity:.8}75%{transform:rotateZ(-5deg);opacity:1}100%{transform:rotateX(0);opacity:1}}@keyframes swal2-toast-show{0%{transform:translateY(-0.625em) rotateZ(2deg)}33%{transform:translateY(0) rotateZ(-2deg)}66%{transform:translateY(0.3125em) rotateZ(2deg)}100%{transform:translateY(0) rotateZ(0deg)}}@keyframes swal2-toast-hide{100%{transform:rotateZ(1deg);opacity:0}}@keyframes swal2-toast-animate-success-line-tip{0%{top:.5625em;left:.0625em;width:0}54%{top:.125em;left:.125em;width:0}70%{top:.625em;left:-0.25em;width:1.625em}84%{top:1.0625em;left:.75em;width:.5em}100%{top:1.125em;left:.1875em;width:.75em}}@keyframes swal2-toast-animate-success-line-long{0%{top:1.625em;right:1.375em;width:0}65%{top:1.25em;right:.9375em;width:0}84%{top:.9375em;right:0;width:1.125em}100%{top:.9375em;right:.1875em;width:1.375em}}');
export {
  Swal as default
};
/*! Bundled license information:

sweetalert2/dist/sweetalert2.esm.all.js:
  (*!
  * sweetalert2 v11.22.5
  * Released under the MIT License.
  *)
*/
//# sourceMappingURL=sweetalert2.js.map
