import { Route } from '@angular/router';
import { FeatureAuth } from 'feature-auth';
import { FeatureContacts } from 'feature-contacts';

export const appRoutes: Route[] = [
  { path: 'auth', component: FeatureAuth },
  { path: 'contacts', component: FeatureContacts },
  { path: 'contacts/info', component: FeatureContacts },
  { path: 'contacts/create', component: FeatureContacts },
  { path: 'contacts/edit', component: FeatureContacts },
  { path: '', pathMatch: 'full', redirectTo: 'auth' },
  { path: '**', redirectTo: 'auth' },
];
