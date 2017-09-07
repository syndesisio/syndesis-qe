// export enum IntegrationStatus {
//   'View',  
//   'Active',
//   'Inactive',
//   'Draft',
//   'In Progress',
//   'Deleted'
// }

export class IntegrationsUtils {

  // to be done: change know types to enum
  static getProperKebabActions(status: string): string[] {

    const view = 'View';
    const edit = 'Edit';
    const deactivate = 'Deactivate';
    const activate = 'Activate';
    const deleteAction = 'Delete';

    switch (status) {
      case 'Active': {
        return [view, edit, deactivate, deleteAction];
      }
      case 'Inactive': {
        return [view, edit, activate, deleteAction];
      }
      case 'Draft': {
        return [view, edit, activate, deleteAction];
      }
      case 'In Progress': {
        return [view, edit, deleteAction];
      }
      default: {
        return [];
      }
    }
  }
}
