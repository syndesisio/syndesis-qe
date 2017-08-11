// export enum IntegrationStatus {
//   'Active',
//   'Inactive',
//   'Draft',
//   'In Progress',
//   'Deleted'
// }

export class IntegrationsUtils {

  // to be done: change know types to enum
  static getProperKebabActions(status: string): string[] {

    const edit = 'Edit';
    const deactivate = 'Deactivate';
    const activate = 'Activate';
    const deletee = 'Delete';

    switch (status) {
      case 'Active': {
        return [edit, deactivate, deletee];
      }
      case 'Inactive': {
        return [edit, activate, deletee];
      }
      case 'Draft':
      case 'In Progress': {
        return [edit, deletee];
      }
      default: {
        return [];
      }
    }
  }
}
