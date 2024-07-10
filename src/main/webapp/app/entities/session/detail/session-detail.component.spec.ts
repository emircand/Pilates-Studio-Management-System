import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { SessionDetailComponent } from './session-detail.component';

describe('Session Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SessionDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: SessionDetailComponent,
              resolve: { session: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(SessionDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load session on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', SessionDetailComponent);

      // THEN
      expect(instance.session).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
