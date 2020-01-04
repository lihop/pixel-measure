import React from 'react';
import {create} from 'mobx-persist';
import AsyncStorage from '@react-native-community/async-storage';
import ImageStore from './ImageStore';

const hydrate = create({storage: AsyncStorage});
hydrate('imageStore', ImageStore);

const storesContext = React.createContext({
  image: ImageStore,
});

export function useStores() {
  return React.useContext(storesContext);
}
