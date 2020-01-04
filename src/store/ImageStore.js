import {observable, action} from 'mobx';
import {persist} from 'mobx-persist';
import {concat} from 'lodash';

class ImageStore {
  @persist('list') @observable images = [];

  @action
  addImage(uri) {
    console.info('Adding image: ', uri);
    this.images = concat(this.images, [{uri}]);
  }
}

export default new ImageStore();
