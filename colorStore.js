import React from 'react';
import {observable, action} from 'mobx';

class ColorStore {
  @observable color = 'red';

  @action
  setColor(color) {
    this.color = color;
  }
}

const colorStore = new ColorStore();

const storesContext = React.createContext({
  color: colorStore,
});

export const useStores = () => {
  return React.useContext(storesContext);
};
